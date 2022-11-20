package dev.sheldan.abstracto.experience.job;

import dev.sheldan.abstracto.experience.service.AUserExperienceService;
import dev.sheldan.abstracto.experience.service.RunTimeExperienceService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;


/**
 * This {@link QuartzJobBean job} is executed regularly and calls the the {@link AUserExperienceService service}
 * store the tracked experience from runtime. This job also cleans up the already processed entries in the runtime
 * experience.
 */
@Slf4j
@DisallowConcurrentExecution
@Component
@PersistJobDataAfterExecution
public class ExperienceCleanupJob extends QuartzJobBean {

    @Autowired
    private RunTimeExperienceService runTimeExperienceService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        runTimeExperienceService.takeLock();
        log.info("Cleaning up experience runtime storage.");
        try {
            runTimeExperienceService.cleanupRunTimeStorage();
        } finally {
            runTimeExperienceService.releaseLock();
        }
    }


}

