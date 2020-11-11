package dev.sheldan.abstracto.statistic.emotes.job;

import dev.sheldan.abstracto.statistic.emotes.model.PersistingEmote;
import dev.sheldan.abstracto.statistic.emotes.service.TrackedEmoteRuntimeService;
import dev.sheldan.abstracto.statistic.emotes.service.TrackedEmoteService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@DisallowConcurrentExecution
@Component
@PersistJobDataAfterExecution
public class EmotePersistingJob extends QuartzJobBean {

    @Autowired
    private TrackedEmoteRuntimeService trackedEmoteRuntimeService;

    @Autowired
    private TrackedEmoteService trackedEmoteService;

    @Override
    @Transactional
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        Map<Long, Map<Long, List<PersistingEmote>>> runtimeConfig = trackedEmoteRuntimeService.getRuntimeConfig();
        log.info("Running statistic persisting job.");
        Long pastMinute = getPastMinute();
        if(runtimeConfig.containsKey(pastMinute)) {
            Map<Long, List<PersistingEmote>> foundStatistics = runtimeConfig.get(pastMinute);
            log.info("Found emote statistics from {} servers to persist.", foundStatistics.size());
            trackedEmoteService.storeEmoteStatistics(foundStatistics);
            runtimeConfig.remove(pastMinute);
            checkForPastEmoteStats(pastMinute, runtimeConfig);
        }
    }

    private void checkForPastEmoteStats(Long minuteToCheck, Map<Long, Map<Long, List<PersistingEmote>>> runtimeConfig) {
        List<Long> missedMinutes = runtimeConfig.keySet().stream().filter(aLong -> aLong < minuteToCheck).collect(Collectors.toList());
        missedMinutes.forEach(pastMinute -> {
            log.info("Persisting emotes for a minute in the past, it should have been previously, but was not. Minute {}.", pastMinute);
            trackedEmoteService.storeEmoteStatistics(runtimeConfig.get(pastMinute));
            runtimeConfig.remove(pastMinute);
        });
    }

    public long getPastMinute() {
        return (Instant.now().getEpochSecond() / 60) - 1;
    }
}
