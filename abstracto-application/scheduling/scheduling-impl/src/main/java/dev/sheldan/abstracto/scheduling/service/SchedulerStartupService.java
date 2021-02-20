package dev.sheldan.abstracto.scheduling.service;

import dev.sheldan.abstracto.scheduling.service.management.SchedulerJobManagementServiceBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Component
public class SchedulerStartupService {

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private SchedulerJobManagementServiceBean schedulerJobManagementServiceBean;

    /**
     * Loads the job definitions from the database and schedules them, if the job does not exist yet.
     */
    @EventListener
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void handleContextRefreshEvent(ContextRefreshedEvent ctxStartEvt) {
        schedulerJobManagementServiceBean.findAll().forEach(schedulerJob -> {
            if(!schedulerJobManagementServiceBean.doesJobExist(schedulerJob) || !schedulerJobManagementServiceBean.isJobDefinitionTheSame(schedulerJob)) {
                schedulerJobManagementServiceBean.createOrUpdate(schedulerJob);
            }
        });
        schedulerService.startScheduledJobs();
    }
}
