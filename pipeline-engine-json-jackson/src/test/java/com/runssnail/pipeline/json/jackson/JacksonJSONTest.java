package com.runssnail.pipeline.json.jackson;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.junit.Test;

import com.runssnail.pipeline.api.spi.JSON;

/**
 * @author zhengwei
 * Created on 2020-09-14
 */
public class JacksonJSONTest {

    @Test
    public void testJson() {

        ServiceLoader<JSON> stepFactories = ServiceLoader.load(JSON.class);
        Iterator<JSON> iterator = stepFactories.iterator();
        if (iterator.hasNext()) {
            System.out.println("has next");
        }

        while (iterator.hasNext()) {
            JSON json = iterator.next();
//            System.out.println(json.getType());
            System.out.println(json);
        }
    }
}
