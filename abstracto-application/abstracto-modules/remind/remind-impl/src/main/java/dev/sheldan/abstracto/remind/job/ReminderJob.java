package dev.sheldan.abstracto.remind.job;

import dev.sheldan.abstracto.remind.service.ReminderService;
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
public class ReminderJob extends QuartzJobBean {

    private Long reminderId;

    @Autowired
    private ReminderService reminderService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            log.info("Executing reminder job for reminder {}", reminderId);
            reminderService.executeReminder(reminderId);
        } catch (Exception e) {
            log.error("Reminder job failed to execute.", e);
        }
    }

    public Long getReminderId() {
        return reminderId;
    }

    public void setReminderId(Long reminderId) {
        this.reminderId = reminderId;
    }
}
