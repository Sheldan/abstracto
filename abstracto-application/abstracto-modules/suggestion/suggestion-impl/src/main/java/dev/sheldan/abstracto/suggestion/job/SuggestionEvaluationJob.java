package dev.sheldan.abstracto.suggestion.job;

import dev.sheldan.abstracto.suggestion.service.SuggestionService;
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
@Getter
@Setter
@PersistJobDataAfterExecution
public class SuggestionEvaluationJob extends QuartzJobBean {

    private Long suggestionId;
    private Long serverId;

    @Autowired
    private SuggestionService suggestionService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("Executing suggestion evaluation job for suggestion {} in server {}.", suggestionId, serverId);
        try {
            suggestionService.evaluateSuggestion(serverId, suggestionId)
                    .thenAccept(unused -> {
                        log.info("Finished evaluation suggestion {} in server {}.", suggestionId, serverId);
                    })
                    .exceptionally(throwable -> {
                        log.error("Failed to evaluate suggestion {} in server {}.", suggestionId, serverId, throwable);
                        return null;
                    });
        } catch (Exception exception) {
            log.error("Suggestion evaluation up job failed.", exception);
        }
    }
}
