package dev.sheldan.abstracto.modmail.job;

import dev.sheldan.abstracto.modmail.service.ModMailThreadServiceBean;
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
public class ModmailThreadActionJob extends QuartzJobBean {

    @Autowired
    private ModMailThreadServiceBean modMailThreadServiceBean;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            log.info("Check modmail threads to perform action for.");
            modMailThreadServiceBean.checkModmailActionsForNeededActions();
        } catch (Exception exception) {
            log.error("Modmail thread action job failed.", exception);
        }

    }
}
