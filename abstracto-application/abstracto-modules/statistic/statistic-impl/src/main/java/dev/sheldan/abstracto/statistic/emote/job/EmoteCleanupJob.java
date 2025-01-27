package dev.sheldan.abstracto.statistic.emote.job;

import dev.sheldan.abstracto.statistic.emote.service.RunTimeReactionEmotesService;
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
public class EmoteCleanupJob extends QuartzJobBean {

    @Autowired
    private RunTimeReactionEmotesService runtimeReactionEmotesService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("Cleaning up emote runtime storage.");
        try {
            runtimeReactionEmotesService.cleanupRunTimeStorage();
        } catch (Exception e) {
            log.error("Failed to cleanup reaction runtimes.", e);
        }
    }


}

