package dev.sheldan.abstracto.core.interaction.job;

import dev.sheldan.abstracto.core.command.CommandReceivedHandler;
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
@Setter
public class ConfirmationCleanupJob extends QuartzJobBean {

    private Long serverId;
    private Long channelId;
    private Long messageId;
    private String confirmationPayloadId;
    private String abortPayloadId;

    @Autowired
    private CommandReceivedHandler commandReceivedHandler;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("Cleaning up confirmation message {} in server {} in channel {}.", messageId, serverId, channelId);
        commandReceivedHandler.cleanupConfirmationMessage(serverId, channelId, messageId, confirmationPayloadId, abortPayloadId)
        .thenAccept(unused -> log.info("Deleted confirmation message {}", messageId))
        .exceptionally(throwable -> {
            log.warn("Failed to cleanup confirmation message {}.", messageId);
            return null;
        });
    }
}
