package com.runssnail.pipeline.definition.mysql;

import java.util.List;
import java.util.Map;

import com.runssnail.pipeline.api.metadata.BaseStepDefinitionRepository;
import com.runssnail.pipeline.api.metadata.StepDefinition;

/**
 * MysqlStepDefinitionRepository
 *
 * @author zhengwei
 * Created on 2020-09-10
 */
public class MysqlStepDefinitionRepository extends BaseStepDefinitionRepository {

    @Override
    public Map<String, StepDefinition> getStepDefinitions() {
        // todo 待实现
        return null;
    }

    @Override
    public StepDefinition get(String stepId) {
        // todo 待实现
        return null;
    }

    @Override
    public List<StepDefinition> getStepDefinitions(long updateTimeStart) {
        // todo 待实现
        return null;
    }
}
