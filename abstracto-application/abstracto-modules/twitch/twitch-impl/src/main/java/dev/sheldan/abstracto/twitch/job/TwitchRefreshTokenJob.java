package dev.sheldan.abstracto.twitch.job;

import dev.sheldan.abstracto.twitch.service.StreamerService;
import dev.sheldan.abstracto.twitch.service.TwitchService;
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
public class TwitchRefreshTokenJob extends QuartzJobBean {
    @Autowired
    private TwitchService twitchService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("Refreshing twitch token..");
        try {
            twitchService.refreshToken();
        } catch (Exception exception) {
            log.error("Failed to refresh twitch token.", exception);
        }
    }
}
