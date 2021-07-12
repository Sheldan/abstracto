package dev.sheldan.abstracto.activity.job;

import dev.sheldan.abstracto.activity.service.ActivityService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Slf4j
@DisallowConcurrentExecution
@Component
@PersistJobDataAfterExecution
public class DynamicActivityJob extends QuartzJobBean {

    @Autowired
    private ActivityService activityService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("Switching activity.");
        activityService.switchToOtherActivity();
    }
}
