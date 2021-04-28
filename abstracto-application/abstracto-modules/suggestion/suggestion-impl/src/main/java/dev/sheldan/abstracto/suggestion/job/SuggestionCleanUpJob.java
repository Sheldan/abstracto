package dev.sheldan.abstracto.suggestion.job;

import dev.sheldan.abstracto.suggestion.service.SuggestionService;
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
public class SuggestionCleanUpJob extends QuartzJobBean {

    @Autowired
    private SuggestionService suggestionService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("Executing suggestion clean up job.");
        try {
            suggestionService.cleanUpSuggestions();
        } catch (Exception exception) {
            log.error("Suggestion clean up job failed.", exception);
        }
    }
}
