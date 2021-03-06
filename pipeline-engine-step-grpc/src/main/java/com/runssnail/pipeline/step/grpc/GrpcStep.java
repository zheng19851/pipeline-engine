package com.runssnail.pipeline.step.grpc;

import java.util.Map;

import org.apache.commons.lang3.Validate;

import com.runssnail.pipeline.api.BaseStep;
import com.runssnail.pipeline.api.Exchange;
import com.runssnail.pipeline.api.constant.ExchangeAttributeEnum;
import com.runssnail.pipeline.api.exception.ExecuteException;


/**
 * GrpcStep
 *
 * @author zhengwei
 * Created on 2020-09-08
 */
public class GrpcStep extends BaseStep<Map<String, Object>> {

    /**
     * 业务定义
     */
    private String bizDef;

    /**
     * 包名+类名
     */
    private String fullName;

    /**
     * 方法名
     */
    private String method;

    /**
     * 创建GrpcStep
     *
     * @param stepId   步骤唯一标识
     * @param bizDef   业务定义
     * @param fullName 报名/类名（这个是在proto里定义的）
     * @param method   方法名
     */
    public GrpcStep(String stepId, String bizDef, String fullName, String method) {
        super(stepId);
        Validate.notBlank(bizDef, "bizDef is required");
        Validate.notBlank(fullName, "fullName is required");
        Validate.notBlank(method, "method is required");
        this.bizDef = bizDef;
        this.fullName = fullName;
        this.method = method;
    }

    @Override
    protected void doExecute(Exchange<Map<String, Object>> exchange) throws ExecuteException {
        // 流程ID
        String pipelineId = exchange.getPipelineId();
        // 阶段ID
        String phaseId = exchange.getAttribute(ExchangeAttributeEnum.CURR_PHASE_ID.name()).toString();

        // todo 根据参数映射，从上下文里获取对应的请求参数，并组装成gRPC请求参数(参数映射)
        // 从缓存中获取参数映射配置
        // 根据pipelineId phaseId stepId找到对应的参数映射，一个流程里，可能存在一个不同的阶段调用相同的服务

        Map<String, Object> body = exchange.getBody();

        // todo 调gRPC服务

        // 将grpc结果通过参数映射设置到上下文中

    }

    public String getBizDef() {
        return bizDef;
    }

    public void setBizDef(String bizDef) {
        this.bizDef = bizDef;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
