package dev.sheldan.abstracto.linkembed.job;

import dev.sheldan.abstracto.linkembed.service.MessageEmbedService;
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
public class LinkEmbedCleanupJob extends QuartzJobBean {

    @Autowired
    private MessageEmbedService messageEmbedService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            log.info("Executing link embed clean up job.");
            messageEmbedService.cleanUpOldMessageEmbeds();
        } catch (Exception exception) {
            log.error("Link embed cleanup job failed.", exception);
        }
    }
}
