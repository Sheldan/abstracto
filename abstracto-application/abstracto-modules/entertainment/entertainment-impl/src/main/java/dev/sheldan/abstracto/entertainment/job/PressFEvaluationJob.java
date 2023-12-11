package dev.sheldan.abstracto.entertainment.job;

import dev.sheldan.abstracto.entertainment.service.EntertainmentService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Slf4j
@DisallowConcurrentExecution
@Component
@PersistJobDataAfterExecution
public class PressFEvaluationJob extends QuartzJobBean {
    @Getter
    @Setter
    private Long pressFId;

    @Autowired
    private EntertainmentService entertainmentService;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        try {
            log.info("Executing press f evaluation job for pressf instance {}.", pressFId);
            entertainmentService.evaluatePressF(pressFId);
        } catch (Exception exception) {
            log.error("Press f evaluation job failed.", exception);
        }
    }
}
