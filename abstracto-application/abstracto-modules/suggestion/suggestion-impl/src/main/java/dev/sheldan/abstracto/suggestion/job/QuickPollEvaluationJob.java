package dev.sheldan.abstracto.suggestion.job;

import dev.sheldan.abstracto.suggestion.service.PollService;
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
public class QuickPollEvaluationJob extends QuartzJobBean {

    private Long pollId;
    private Long serverId;

    @Autowired
    private PollService pollService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("Executing poll evaluation job for quick poll {} in server {}.", pollId, serverId);
        try {
            pollService.evaluateQuickPoll(pollId, serverId).thenAccept(unused -> {
                log.info("Evaluated quick poll {} in server {}.", pollId, serverId);
            }).exceptionally(throwable -> {
                log.error("Failed to evaluate quick poll {} in server {}.", pollId, serverId, throwable);
                return null;
            });
        } catch (Exception exception) {
            log.error("Quick poll evaluation job failed.", exception);
        }
    }
}
