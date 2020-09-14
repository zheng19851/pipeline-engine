package com.runssnail.pipeline.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.runssnail.pipeline.api.Phase;
import com.runssnail.pipeline.api.PhaseFactory;
import com.runssnail.pipeline.api.PhaseRepository;
import com.runssnail.pipeline.api.Pipeline;
import com.runssnail.pipeline.api.PipelineFactory;
import com.runssnail.pipeline.api.PipelineRepository;
import com.runssnail.pipeline.api.Step;
import com.runssnail.pipeline.api.StepFactoryRepository;
import com.runssnail.pipeline.api.StepRepository;
import com.runssnail.pipeline.api.concurrent.DefaultExecutorFactory;
import com.runssnail.pipeline.api.spi.ExecutorFactory;
import com.runssnail.pipeline.api.constant.Constants;
import com.runssnail.pipeline.api.exception.PipelineDefinitionException;
import com.runssnail.pipeline.api.exception.StepDefinitionException;
import com.runssnail.pipeline.api.metadata.PhaseDefinition;
import com.runssnail.pipeline.api.metadata.PipelineDefinition;
import com.runssnail.pipeline.api.metadata.PipelineDefinitionRepository;
import com.runssnail.pipeline.api.metadata.StepDefinition;
import com.runssnail.pipeline.api.spi.StepFactory;

/**
 * DefaultPipelineRepository
 * <p>
 * 用本地内存实现Pipeline缓存
 *
 * @author zhengwei
 */
@Repository
public class MemoryPipelineRepository implements PipelineRepository {
    private static final Logger log = LoggerFactory.getLogger(MemoryPipelineRepository.class);

    /**
     * 调度周期，单位毫秒
     */
    private long scheduledPeriod = Constants.DEFAULT_SCHEDULED_PERIOD;

    /**
     * key=pipelineId
     */
    private ConcurrentMap<String, Pipeline> pipelines = new ConcurrentHashMap<>();

    /**
     * 流程定义仓储
     */
    @Autowired
    private PipelineDefinitionRepository pipelineDefinitionRepository;

    /**
     * 流程工厂
     */
    @Autowired
    private PipelineFactory pipelineFactory;

    /**
     * 阶段工厂
     */
    @Autowired
    private PhaseFactory phaseFactory;

    /**
     * 阶段仓储
     */
    @Autowired
    private PhaseRepository phaseRepository;

    /**
     * StepFactory仓储
     */
    @Autowired
    private StepFactoryRepository stepFactoryRepository;

    /**
     * 阶段仓储
     */
    @Autowired
    private StepRepository stepRepository;

    /**
     * 定时刷新缓存用
     */
    private ScheduledExecutorService scheduledExecutorService;

    /**
     * 线程创建工厂
     */
    private ExecutorFactory executorFactory;

    /**
     * 当前更新的流程定义最新时间
     */
    private volatile long lastUpdateTime;

    /**
     * Default constructor
     */
    public MemoryPipelineRepository() {
    }

    /**
     * @param pipelineId
     * @return
     */
    @Override
    public Pipeline getPipeline(String pipelineId) {
        return pipelines.get(pipelineId);
    }

    /**
     * @param pipelines
     */
    @Override
    public void saveAll(List<Pipeline> pipelines) {
        for (Pipeline pipeline : pipelines) {
            this.save(pipeline);
        }
    }

    /**
     * @param pipeline
     */
    @Override
    public void save(Pipeline pipeline) {
        log.info("save Pipeline {}", pipeline.getPipelineId());
        pipelines.put(pipeline.getPipelineId(), pipeline);
    }

    /**
     * @param pipelineId 流程唯一标识
     * @return
     */
    @Override
    public Pipeline remove(String pipelineId) {
        log.info("remove Pipeline {}", pipelineId);
        return pipelines.remove(pipelineId);
    }

    /**
     * @param pipelineIds 流程唯一标识
     * @return
     */
    @Override
    public List<Pipeline> removeAll(List<String> pipelineIds) {
        log.info("removeAll Pipeline {}", pipelineIds);
        List<Pipeline> removedList = new ArrayList<>(pipelineIds.size());
        for (String pipelineId : pipelineIds) {
            Pipeline pipeline = this.remove(pipelineId);
            if (pipeline != null) {
                removedList.add(pipeline);
            }
        }
        return removedList;
    }

    @Override
    public boolean contains(String pipelineId) {
        return this.pipelines.containsKey(pipelineId);
    }

    @Override
    public List<Pipeline> getAllPipelines() {
        return new ArrayList<>(this.pipelines.values());
    }

    @Override
    public void init() {
        Validate.notNull(this.pipelineDefinitionRepository, "PipelineDefinitionRepository is required");
        Validate.notNull(this.pipelineFactory, "pipelineFactory is required");
        Validate.notNull(this.phaseFactory, "phaseFactory is required");
        Validate.notNull(this.phaseRepository, "phaseRepository is required");
        Validate.notNull(this.stepFactoryRepository, "stepFactoryRepository is required");
        Validate.notNull(this.stepRepository, "stepRepository is required");

        log.info("init start");
        initExecutorFactory();
        refreshPipelines(true);
        initRefreshThread();
        log.info("init end");
    }

    private void initExecutorFactory() {
        if (this.executorFactory == null) {
            this.executorFactory = new DefaultExecutorFactory();
        }
    }

    private void initRefreshThread() {
        initRefreshPipelineDefinitionThread();
    }

    private void initRefreshPipelineDefinitionThread() {

        if (this.scheduledExecutorService == null) {
            // todo 参数配置化
            ScheduledExecutorService executor = executorFactory.createScheduled(Constants.DEFAULT_SCHEDULED_CORE_POOL_SIZE, "RefreshPipeline");

            // 每隔一段时间去刷新
            // todo period参数配置化
            executor.scheduleAtFixedRate(new RefreshPipelineDefinitionTask(), scheduledPeriod, scheduledPeriod, TimeUnit.SECONDS);
            this.scheduledExecutorService = executor;
            log.info("init RefreshPipelineDefinitionThread end");
        }

    }

    private class RefreshPipelineDefinitionTask implements Runnable {

        @Override
        public void run() {
            try {
                refreshPipelineDefinition();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void refreshPipelineDefinition() {
        this.refreshPipelines(false);
    }

    private void refreshPipelines(boolean onlyEnabled) {
        // 保存当前更新时间
        long lastUpdateTime = this.lastUpdateTime;
        List<PipelineDefinition> pipelineDefinitions = pipelineDefinitionRepository.getPipelineDefinitions(onlyEnabled, lastUpdateTime);
        if (CollectionUtils.isEmpty(pipelineDefinitions)) {
            log.warn("Cannot find any pipelineDefinitions, lastUpdateTime={}", lastUpdateTime);
            return;
        }

        if (onlyEnabled) {
            // 初始化时，需要校验下唯一标识是否重复
            this.validate(pipelineDefinitions);
        }

        for (PipelineDefinition pd : pipelineDefinitions) {
            if (pd.isRemoved()) {
                // 如果Phase是不能重用的，那么删除Pipeline的同时需要删除Phase，否则存在内存泄漏问题
                this.remove(pd.getPipelineId());
            } else {
                Pipeline pipeline = pipelineFactory.create(pd);
                this.save(pipeline);
                refreshPhases(pd.getPhaseDefinitions());
            }

        }

        this.validate();
        resetLastPipelineUpdateTime(pipelineDefinitions);

        log.info("refreshPipelines end, find {}, [{}->{}], {}", pipelineDefinitions.size(), lastUpdateTime, this.lastUpdateTime, pipelineDefinitions);
    }

    /**
     * 验证流程定义是否有效，流程定义里的phase和step是否都存在
     */
    protected void validate() {
    }

    private void resetLastPipelineUpdateTime(List<PipelineDefinition> pipelineDefinitions) {
        for (PipelineDefinition pipelineDefinition : pipelineDefinitions) {
            if (pipelineDefinition.getUpdateTime() > this.lastUpdateTime) {
                lastUpdateTime = pipelineDefinition.getUpdateTime();
            }
        }

    }

    private void refreshPhases(List<PhaseDefinition> phaseDefinitions) {
        if (CollectionUtils.isEmpty(phaseDefinitions)) {
            log.info("phaseDefinitions is empty");
            return;
        }

        log.info("refreshPhases start, find {} PhaseDefinition", phaseDefinitions.size());

        for (PhaseDefinition phaseDefinition : phaseDefinitions) {
            if (phaseDefinition.isRemoved()) {
                phaseRepository.remove(phaseDefinition.getPhaseId());
            } else {
                Phase phase = this.phaseFactory.create(phaseDefinition);
                this.phaseRepository.save(phase);
                refreshSteps(phaseDefinition.getStepDefinitions());
            }
        }

        log.info("refreshPhases end, find {} PhaseDefinition", phaseDefinitions.size());
    }

    private void refreshSteps(List<StepDefinition> stepDefinitions) {
        if (CollectionUtils.isEmpty(stepDefinitions)) {
            log.info("stepDefinitions is empty");
            return;
        }

        log.info("refreshSteps start, find {} stepDefinitions", stepDefinitions.size());

        for (StepDefinition sd : stepDefinitions) {
            if (sd.isRemoved()) {
                stepRepository.remove(sd.getStepId());
            } else {
                StepFactory stepFactory = stepFactoryRepository.getStepFactory(sd.getStepType());
                if (stepFactory == null) {
                    throw new StepDefinitionException("Cannot find StepFactory stepId=" + sd.getStepId() + ", type=" + sd.getStepType());
                }
                Step step = stepFactory.create(sd);
                this.stepRepository.save(step);
            }
        }
        log.info("refreshSteps end, find {} stepDefinitions", stepDefinitions.size());
    }

    /**
     * 验证是否重复
     *
     * @param pipelineDefinitions
     */
    private void validate(List<PipelineDefinition> pipelineDefinitions) {
        for (PipelineDefinition pipelineDefinition : pipelineDefinitions) {
            String pipelineId = pipelineDefinition.getPipelineId();
            if (this.contains(pipelineId)) {
                throw new PipelineDefinitionException(pipelineId, "pipeline id duplicated '" + pipelineId + "'");
            }
        }

    }

    @Override
    public void close() {
        if (this.scheduledExecutorService != null) {
            this.scheduledExecutorService.shutdown();
        }
    }

    public PipelineDefinitionRepository getPipelineDefinitionRepository() {
        return pipelineDefinitionRepository;
    }

    public void setPipelineDefinitionRepository(PipelineDefinitionRepository pipelineDefinitionRepository) {
        this.pipelineDefinitionRepository = pipelineDefinitionRepository;
    }

    public PipelineFactory getPipelineFactory() {
        return pipelineFactory;
    }

    public void setPipelineFactory(PipelineFactory pipelineFactory) {
        this.pipelineFactory = pipelineFactory;
    }

    public PhaseFactory getPhaseFactory() {
        return phaseFactory;
    }

    public void setPhaseFactory(PhaseFactory phaseFactory) {
        this.phaseFactory = phaseFactory;
    }

    public PhaseRepository getPhaseRepository() {
        return phaseRepository;
    }

    public void setPhaseRepository(PhaseRepository phaseRepository) {
        this.phaseRepository = phaseRepository;
    }

    public StepRepository getStepRepository() {
        return stepRepository;
    }

    public void setStepRepository(StepRepository stepRepository) {
        this.stepRepository = stepRepository;
    }

    public ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    public void setScheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
        this.scheduledExecutorService = scheduledExecutorService;
    }

    public ExecutorFactory getExecutorFactory() {
        return executorFactory;
    }

    public void setExecutorFactory(ExecutorFactory executorFactory) {
        this.executorFactory = executorFactory;
    }

    public StepFactoryRepository getStepFactoryRepository() {
        return stepFactoryRepository;
    }

    public void setStepFactoryRepository(StepFactoryRepository stepFactoryRepository) {
        this.stepFactoryRepository = stepFactoryRepository;
    }
}