package com.runssnail.pipeline.api;

import java.util.List;

/**
 * 流程引擎
 *
 * @author zhengwei
 */
public interface PipelineEngine extends Lifecycle {

    /**
     * 获取流程执行对象
     *
     * @param pipelineId 唯一标识
     * @return
     */
    Pipeline getPipeline(String pipelineId);

    /**
     * 所有流程执行对象
     *
     * @return 所有流程执行对象
     */
    List<Pipeline> getAllPipelines();

    /**
     * 异常处理器
     *
     * @param handler 异常处理器
     * @see DefaultPipelineErrorHandler
     */
    void setPipelineErrorHandler(PipelineErrorHandler handler);

    /**
     * 错误处理器
     *
     * @return 错误处理器
     */
    PipelineErrorHandler getPipelineErrorHandler();

    /**
     * 执行
     *
     * @param exchange 上下文
     */
    void execute(Exchange exchange);

}