package dev.sheldan.abstracto.moderation.job;

import dev.sheldan.abstracto.moderation.service.MuteService;
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
public class UnMuteJob extends QuartzJobBean {

    private Long muteId;

    @Autowired
    private MuteService muteService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("Executing unmute job for mute {}", muteId);
        muteService.endMute(muteId);
    }

    public Long getMuteId() {
        return muteId;
    }

    public void setMuteId(Long muteId) {
        this.muteId = muteId;
    }
}
