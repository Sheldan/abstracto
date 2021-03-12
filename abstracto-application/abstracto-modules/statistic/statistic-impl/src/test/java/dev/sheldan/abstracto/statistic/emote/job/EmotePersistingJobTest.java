package dev.sheldan.abstracto.statistic.emote.job;

import dev.sheldan.abstracto.statistic.emote.model.PersistingEmote;
import dev.sheldan.abstracto.statistic.emote.service.TrackedEmoteRuntimeService;
import dev.sheldan.abstracto.statistic.emote.service.TrackedEmoteService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EmotePersistingJobTest {

    @InjectMocks
    @Spy
    private EmotePersistingJob testUnit;

    @Mock
    private TrackedEmoteRuntimeService trackedEmoteRuntimeService;

    @Mock
    private TrackedEmoteService trackedEmoteService;

    @Mock
    private JobExecutionContext executionContext;

    @Test
    public void testExecuteNoEmoteStats() throws JobExecutionException {
        when(trackedEmoteRuntimeService.getRuntimeConfig()).thenReturn(new HashMap<>());
        testUnit.executeInternal(executionContext);
        verify(trackedEmoteService, times(0)).storeEmoteStatistics(any());
    }

    @Test
    public void testExecuteWithStats() throws JobExecutionException {
        Map<Long, Map<Long, List<PersistingEmote>>> emoteStats = new HashMap<>();
        long minuteToPersist = 4L;
        Map<Long, List<PersistingEmote>> statsForMinute = new HashMap<>();
        statsForMinute.put(8L, new ArrayList<>());
        emoteStats.put(minuteToPersist, statsForMinute);
        when(trackedEmoteRuntimeService.getRuntimeConfig()).thenReturn(emoteStats);
        when(testUnit.getPastMinute()).thenReturn(minuteToPersist);
        testUnit.executeInternal(executionContext);
        verify(trackedEmoteService, times(1)).storeEmoteStatistics(any());
    }

    @Test
    public void testExecuteWithPastStats() throws JobExecutionException {
        Map<Long, Map<Long, List<PersistingEmote>>> emoteStats = new HashMap<>();
        long minuteToPersist = 4L;
        Map<Long, List<PersistingEmote>> statsForMinute = new HashMap<>();
        statsForMinute.put(8L, new ArrayList<>());
        emoteStats.put(minuteToPersist, statsForMinute);
        emoteStats.put(minuteToPersist - 2, statsForMinute);
        when(trackedEmoteRuntimeService.getRuntimeConfig()).thenReturn(emoteStats);
        when(testUnit.getPastMinute()).thenReturn(minuteToPersist);
        testUnit.executeInternal(executionContext);
        verify(trackedEmoteService, times(2)).storeEmoteStatistics(any());
    }

}
