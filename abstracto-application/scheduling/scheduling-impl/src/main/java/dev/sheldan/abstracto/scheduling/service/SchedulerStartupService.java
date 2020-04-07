package dev.sheldan.abstracto.scheduling.service;

import dev.sheldan.abstracto.scheduling.config.JobConfigLoader;
import dev.sheldan.abstracto.scheduling.factory.SchedulerJobConverter;
import dev.sheldan.abstracto.scheduling.model.database.SchedulerJob;
import dev.sheldan.abstracto.scheduling.service.management.SchedulerJobManagementServiceBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Component
public class SchedulerStartupService {

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private JobConfigLoader jobConfigLoader;

    @Autowired
    private SchedulerJobManagementServiceBean schedulerJobManagementServiceBean;

    @Autowired
    private SchedulerJobConverter schedulerJobConverter;

    @EventListener
    @Transactional
    public void handleContextRefreshEvent(ContextRefreshedEvent ctxStartEvt) {
        jobConfigLoader.getJobs().forEach((s, schedulerJob) -> {
            SchedulerJob job = schedulerJobConverter.fromJobProperties(schedulerJob);
            if(!schedulerJobManagementServiceBean.doesJobExist(job) || !schedulerJobManagementServiceBean.isJobDefinitionTheSame(job)) {
                schedulerJobManagementServiceBean.createOrUpdate(job);
            }
        });
        schedulerService.startScheduledJobs();
    }
}
