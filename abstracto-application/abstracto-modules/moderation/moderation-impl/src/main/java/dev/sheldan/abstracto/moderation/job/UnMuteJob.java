package dev.sheldan.abstracto.moderation.job;

import dev.sheldan.abstracto.moderation.service.MuteService;
import lombok.Getter;
import lombok.Setter;
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
@Getter
@Setter
public class UnMuteJob extends QuartzJobBean {

    private Long muteId;
    private Long serverId;

    @Autowired
    private MuteService muteService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("Executing unMute job for mute {} in server {}", muteId, serverId);
        muteService.endMute(muteId, serverId);
    }

}
