package com.runssnail.pipeline.step.bean;

import java.util.List;

import com.runssnail.pipeline.api.BaseStepFactory;
import com.runssnail.pipeline.api.Interceptor;
import com.runssnail.pipeline.api.Step;
import com.runssnail.pipeline.api.exception.StepDefinitionException;
import com.runssnail.pipeline.api.metadata.StepDefinition;
import com.runssnail.pipeline.api.spi.StepFactory;

/**
 * 步骤执行对象工厂
 *
 * @author zhengwei
 * Created on 2020-09-08
 * @see BeanStep
 */
public class BeanStepFactory extends BaseStepFactory implements StepFactory {

    @Override
    public Step doCreate(StepDefinition definition) throws StepDefinitionException {

        Step step = null;
        if ("bean".equalsIgnoreCase(definition.getStepType())) {
            return createBeanStep(definition);
        }

        return step;
    }

    private BeanStep createBeanStep(StepDefinition definition) {
        BeanStep beanStep = new BeanStep(definition.getStepId());
        beanStep.setInterceptors(createInterceptors(definition));
        return beanStep;
    }

    private List<Interceptor> createInterceptors(StepDefinition definition) {
        // 预留
        return null;
    }

    @Override
    public String getType() {
        return "bean";
    }
}
