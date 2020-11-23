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

/**
 * Job responsible for persisting the emote usages found in {@link TrackedEmoteRuntimeService} to the database.
 * This will create new instances, if there are non before, and increment past instances. This job runs for all servers globally.
 */
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
        // acquire the lock, because we are modifying
        trackedEmoteRuntimeService.takeLock();
        Long pastMinute = getPastMinute();
        Map<Long, Map<Long, List<PersistingEmote>>> runtimeConfig = trackedEmoteRuntimeService.getRuntimeConfig();
        try {
            log.info("Running statistic persisting job.");
            // only take the emotes from the last minute, if there are any
            if(runtimeConfig.containsKey(pastMinute)) {
                Map<Long, List<PersistingEmote>> foundStatistics = runtimeConfig.get(pastMinute);
                log.info("Found emote statistics from {} servers to persist.", foundStatistics.size());
                trackedEmoteService.storeEmoteStatistics(foundStatistics);
                // remove it, because we processed it
                // check for earlier entries which were missed
                checkForPastEmoteStats(pastMinute, runtimeConfig);
            }
        } finally {
            runtimeConfig.remove(pastMinute);
            // release the lock, so other listeners can add onto it again
            trackedEmoteRuntimeService.releaseLock();
        }
    }

    private void checkForPastEmoteStats(Long minuteToCheck, Map<Long, Map<Long, List<PersistingEmote>>> runtimeConfig) {
        // if there are any keys which have a lower minute, we need to process them, because they most likely have not been processed yet
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
