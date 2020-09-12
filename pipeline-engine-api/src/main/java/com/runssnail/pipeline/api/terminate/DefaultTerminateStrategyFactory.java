package com.runssnail.pipeline.api.terminate;

import com.runssnail.pipeline.api.exception.PipelineDefinitionException;

/**
 * 默认中断策略工厂
 *
 * @author zhengwei
 * Created on 2020-09-12
 */
public class DefaultTerminateStrategyFactory implements TerminateStrategyFactory {
    @Override
    public TerminateStrategy create(String strategy) throws PipelineDefinitionException {
        if (TerminateStrategyEnum.ABORT.name().equalsIgnoreCase(strategy)) {
            return new AbortTerminateStrategy();
        }

        if (TerminateStrategyEnum.LOGGING.name().equalsIgnoreCase(strategy)) {
            return new LoggingTerminateStrategy();
        }

        throw new PipelineDefinitionException("terminate strategy is unsupported '" + strategy + "', use " + TerminateStrategyEnum.values());
    }
}
