package com.runssnail.pipeline.api;

import com.runssnail.pipeline.api.exception.ExecuteException;

/**
 * 阶段异常统一处理器
 *
 * @author zhengwei
 * Created on 2020-09-08
 */
public interface PhaseErrorHandler {

    /**
     * 处理异常
     *
     * @param phase   阶段执行对象
     * @param exchange 上下文
     * @param t       异常
     */
    void onException(Phase phase, Exchange exchange, Throwable t) throws ExecuteException;
}
