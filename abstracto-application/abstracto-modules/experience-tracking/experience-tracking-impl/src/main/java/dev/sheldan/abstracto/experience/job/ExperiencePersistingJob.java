package dev.sheldan.abstracto.experience.job;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.service.ExperienceTrackerService;
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


@Slf4j
@DisallowConcurrentExecution
@Component
@PersistJobDataAfterExecution
public class ExperiencePersistingJob extends QuartzJobBean {

    @Autowired
    private ExperienceTrackerService experienceTrackerService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        HashMap<Long, List<AServer>> runtimeExperience = experienceTrackerService.getRuntimeExperience();
        log.info("Storing experience");
        Long pastMinute = (Instant.now().getEpochSecond() / 60) - 1;
        if(runtimeExperience.containsKey(pastMinute)) {
            experienceTrackerService.handleExperienceGain(runtimeExperience.get(pastMinute));
            runtimeExperience.remove(pastMinute);
        }
    }


}

