package dev.sheldan.abstracto.giveaway.job;

import dev.sheldan.abstracto.giveaway.service.GiveawayService;
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
public class GiveawayEvaluationJob extends QuartzJobBean  {

    @Getter
    @Setter
    private Long giveawayId;

    @Getter
    @Setter
    private Long serverId;

    @Autowired
    private GiveawayService giveawayService;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        try {
            log.info("Executing giveaway evaluation job for giveaway {} in server {}", giveawayId, serverId);
            giveawayService.evaluateGiveaway(giveawayId, serverId);
        } catch (Exception exception) {
            log.error("Giveaway evaluation job failed.", exception);
        }
    }

}
