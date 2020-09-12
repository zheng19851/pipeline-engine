package com.runssnail.pipeline.step.bean;

import com.runssnail.pipeline.api.BaseStep;
import com.runssnail.pipeline.api.Exchange;
import com.runssnail.pipeline.api.exception.ExecuteException;

/**
 * BeanStep
 *
 * @author zhengwei
 * Created on 2020-09-11
 */
public class BeanStep extends BaseStep {

    public BeanStep(String stepId) {
        super(stepId);
    }

    @Override
    protected void doExecute(Exchange exchange) throws ExecuteException {
        System.out.println("bean test");
        exchange.setAttribute("tag", "bean");
    }
}
