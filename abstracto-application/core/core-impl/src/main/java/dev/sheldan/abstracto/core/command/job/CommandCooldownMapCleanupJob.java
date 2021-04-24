package dev.sheldan.abstracto.core.command.job;

import dev.sheldan.abstracto.core.command.service.CommandCoolDownServiceBean;
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
public class CommandCooldownMapCleanupJob extends QuartzJobBean {

    @Autowired
    private CommandCoolDownServiceBean commandCoolDownServiceBean;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            log.info("Executing command cooldown cleanup job.");
            commandCoolDownServiceBean.cleanUpCooldownStorage();
        } catch (Exception exception) {
            log.error("Command cooldown cleanup job failed.", exception);
        }
    }
}
