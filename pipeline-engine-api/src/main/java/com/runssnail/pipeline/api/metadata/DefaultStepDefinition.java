package com.runssnail.pipeline.api.metadata;

/**
 * 步骤定义
 *
 * @author zhengwei
 */
public class DefaultStepDefinition extends BaseDefinition implements StepDefinition {

    /**
     * 唯一标识
     */
    private String stepId;

    /**
     * 类型，grpc/http/bean
     */
    private String stepType;

    /**
     * 步骤配置，根据stepType不同，配置不同
     */
    private String stepConfig;

    @Override
    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    @Override
    public String getStepType() {
        return stepType;
    }

    public void setStepType(String stepType) {
        this.stepType = stepType;
    }

    public String getStepConfig() {
        return stepConfig;
    }

    public void setStepConfig(String stepConfig) {
        this.stepConfig = stepConfig;
    }

}