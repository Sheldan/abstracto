package dev.sheldan.abstracto.twitch.job;

import dev.sheldan.abstracto.twitch.service.StreamerService;
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
public class StreamerOnlineCheckJob extends QuartzJobBean {

    @Autowired
    private StreamerService streamerService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("Executing streamer online check");
        try {
            streamerService.checkAndNotifyAboutOnlineStreamers();
        } catch (Exception exception) {
            log.error("Failed to check online status for streamers.", exception);
        }
    }
}
