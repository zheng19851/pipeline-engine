package com.runssnail.pipeline.api;

import com.runssnail.pipeline.api.exception.ExecuteException;

/**
 * 支持对Step、Phase的执行拦截拦截器
 *
 * @author zhengwei
 * Created on 2020-09-08
 * @see Step
 * @see Phase
 */
public interface Interceptor {

    /**
     * 执行前
     *
     * @param exchange 上下文
     */
    void beforeExecute(Exchange exchange) throws ExecuteException;

    /**
     * 执行后
     *
     * @param exchange 上下文
     */
    void afterExecute(Exchange exchange) throws ExecuteException;
}
