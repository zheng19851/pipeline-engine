package com.runssnail.pipeline.json.jackson;

import java.util.Map;

import com.runssnail.pipeline.api.spi.JSON;

/**
 * jackson
 *
 * @author zhengwei
 * Created on 2020-09-14
 */
public class JacksonJSON implements JSON {

    @Override
    public String toJson(Object obj) {
        return null;
    }

    @Override
    public <T> T fromJson(String json, Class<T> valueType) {
        return null;
    }

    @Override
    public Map<String, Object> fromJson(String json) {
        return null;
    }
}
