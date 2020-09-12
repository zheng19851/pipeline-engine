package com.runssnail.pipeline.api;

import java.util.List;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runssnail.pipeline.api.exception.PipelineExecuteErrorCode;
import com.runssnail.pipeline.api.terminate.AbortTerminateStrategy;
import com.runssnail.pipeline.api.terminate.TerminateStrategy;


/**
 * 默认的流程引擎
 *
 * @author zhengwei
 */
public class DefaultPipelineEngine implements PipelineEngine {
    private static final Logger log = LoggerFactory.getLogger(DefaultPipelineEngine.class);

    /**
     * 流程异常处理器
     */
    private PipelineErrorHandler pipelineErrorHandler;

    /**
     * 流程执行对象仓储
     */
    private PipelineRepository pipelineRepository;

    /**
     * 中断策略
     */
    private TerminateStrategy terminateStrategy;

    /**
     * Default constructor
     */
    public DefaultPipelineEngine() {
    }

    /**
     * 加载流程初始化
     */
    @Override
    public void init() {
        Validate.notNull(this.pipelineRepository, "PipelineRepository is required");

        initPipelineErrorHandler();
        initTerminateStrategy();

        log.info("pipeline engine init end");
    }

    private void initTerminateStrategy() {
        if (this.terminateStrategy == null) {
            this.terminateStrategy = new AbortTerminateStrategy();
        }
    }

    @Override
    public void close() {

    }

    private void initPipelineErrorHandler() {
        if (this.pipelineErrorHandler == null) {
            this.pipelineErrorHandler = new DefaultPipelineErrorHandler();
        }
    }

    @Override
    public void execute(Exchange exchange) {
        Validate.notNull(exchange, "exchange is required");
        Validate.notBlank(exchange.getPipelineId(), "exchange.pipelineId is required");
        Validate.notNull(exchange.getBody(), "exchange.body is required");

        Pipeline pipeline = this.getPipeline(exchange.getPipelineId());
        if (pipeline == null) {
            log.warn("cannot find the Pipeline {}", exchange.getPipelineId());
            exchange.setErrorCode(PipelineExecuteErrorCode.PIPELINE_NOT_EXISTS.getErrorCode());
            exchange.setErrorMsg(PipelineExecuteErrorCode.PIPELINE_NOT_EXISTS.getErrorMsg());
            return;
        }

        TerminateStrategy terminateStrategy = resolveTerminateStrategy(pipeline);
        exchange.setTerminateStrategy(terminateStrategy);
        try {
            pipeline.execute(exchange);
        } catch (Exception e) {
            this.pipelineErrorHandler.onException(exchange, e);
        }

    }

    /**
     * 决定使用哪个TerminateStrategy
     *
     * @param pipeline 流程执行对象
     * @return
     */
    private TerminateStrategy resolveTerminateStrategy(Pipeline pipeline) {
        TerminateStrategy terminateStrategy = pipeline.getTerminateStrategy();
        if (terminateStrategy != null) {
            return terminateStrategy;
        }

        return this.terminateStrategy;
    }

    /**
     * 获取流程执行对象
     *
     * @param pipelineId 流程唯一标识
     * @return
     */
    @Override
    public Pipeline getPipeline(String pipelineId) {
        return pipelineRepository.getPipeline(pipelineId);
    }

    /**
     * @return
     */
    @Override
    public List<Pipeline> getAllPipelines() {
        return this.pipelineRepository.getAllPipelines();
    }

    @Override
    public void setPipelineErrorHandler(PipelineErrorHandler handler) {
        this.pipelineErrorHandler = handler;
    }

    @Override
    public PipelineErrorHandler getPipelineErrorHandler() {
        return pipelineErrorHandler;
    }

    public PipelineRepository getPipelineRepository() {
        return pipelineRepository;
    }

    public void setPipelineRepository(PipelineRepository pipelineRepository) {
        this.pipelineRepository = pipelineRepository;
    }

    public TerminateStrategy getTerminateStrategy() {
        return terminateStrategy;
    }

    public void setTerminateStrategy(TerminateStrategy terminateStrategy) {
        this.terminateStrategy = terminateStrategy;
    }
}