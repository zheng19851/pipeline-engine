package com.runssnail.pipeline.api;

import com.runssnail.pipeline.api.exception.ExecuteException;
import com.runssnail.pipeline.api.terminate.TerminateStrategy;

/**
 * 运行域流程执行对象
 *
 * @author zhengwei
 */
public interface Pipeline extends Lifecycle {

    /**
     * 唯一标识
     *
     * @return 唯一标识
     */
    String getPipelineId();

    /**
     * 执行流程
     *
     * @param exchange 上下文
     * @throws ExecuteException
     */
    void execute(Exchange exchange) throws ExecuteException;

    /**
     * 流程自定义的中断策略
     *
     * @return 中断策略
     */
    TerminateStrategy getTerminateStrategy();

    /**
     * 流程定义的错误统一处理器
     *
     * @return PipelineErrorHandler
     */
    PipelineErrorHandler getPipelineErrorHandler();


}