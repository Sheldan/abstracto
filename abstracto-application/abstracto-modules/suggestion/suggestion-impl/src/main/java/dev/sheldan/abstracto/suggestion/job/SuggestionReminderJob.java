package dev.sheldan.abstracto.suggestion.job;

import dev.sheldan.abstracto.core.models.ServerSpecificId;
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
public class SuggestionReminderJob extends QuartzJobBean {

    @Autowired
    private SuggestionService suggestionService;

    private Long suggestionId;
    private Long serverId;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("Executing suggestion reminder job for suggestion {} in server {}", suggestionId, serverId);
        try {
            suggestionService.remindAboutSuggestion(new ServerSpecificId(serverId, suggestionId));
        } catch (Exception exception) {
            log.error("Suggestion reminder job failed.", exception);
        }
    }

}
