package dev.sheldan.abstracto.experience.job;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.service.AUserExperienceService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;


/**
 * This {@link QuartzJobBean} is executed regularly and calls the the {@link AUserExperienceService}
 * store the tracked experience from runtime. This job also cleans up the already processed entries in the runtime
 * experience.
 */
@Slf4j
@DisallowConcurrentExecution
@Component
@PersistJobDataAfterExecution
public class ExperiencePersistingJob extends QuartzJobBean {

    @Autowired
    private AUserExperienceService userExperienceService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        HashMap<Long, List<AServer>> runtimeExperience = userExperienceService.getRuntimeExperience();
        log.info("Running experience persisting job.");
        Long pastMinute = (Instant.now().getEpochSecond() / 60) - 1;
        if(runtimeExperience.containsKey(pastMinute)) {
            log.info("Found experience to persist.");
            userExperienceService.handleExperienceGain(runtimeExperience.get(pastMinute));
            runtimeExperience.remove(pastMinute);
        }
    }


}

