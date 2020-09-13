package com.runssnail.pipeline.api;

import java.util.concurrent.ConcurrentMap;

/**
 * 执行上下文参数
 * 注意：此对象为大对象
 *
 * @author zhengwei
 */
public class DefaultExchange extends BaseExchange<ConcurrentMap<String, Object>> {
    private static final long serialVersionUID = 2409035761108469697L;

    @Override
    protected Object getValueFromBody(String name) {
        ConcurrentMap<String, Object> body = this.getBody();
        return body != null ? body.get(name) : null;
    }
}
