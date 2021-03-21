package dev.sheldan.abstracto.starboard.listener;

import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.listener.ReactionClearedModel;
import dev.sheldan.abstracto.starboard.model.database.StarboardPost;
import dev.sheldan.abstracto.starboard.service.StarboardService;
import dev.sheldan.abstracto.starboard.service.management.StarboardPostManagementService;
import dev.sheldan.abstracto.starboard.service.management.StarboardPostReactorManagementService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StarClearedListenerTest {

    @InjectMocks
    private StarClearedListener testUnit;

    @Mock
    private StarboardService starboardService;

    @Mock
    private StarboardPostManagementService starboardPostManagementService;

    @Mock
    private StarboardPostReactorManagementService starboardPostReactorManagementService;

    @Mock
    private MetricService metricService;

    @Mock
    private CachedMessage cachedMessage;

    @Mock
    private ReactionClearedModel model;

    private static final Long MESSAGE_ID = 5L;

    @Test
    public void testReactionsClearedOnStarredMessage() {
        executeClearingTest(Mockito.mock(StarboardPost.class));
    }

    @Test
    public void testReactionsClearedOnNotStarredMessage() {
        executeClearingTest(null);
    }

    private void executeClearingTest(StarboardPost post) {
        when(cachedMessage.getMessageId()).thenReturn(MESSAGE_ID);
        when(model.getMessage()).thenReturn(cachedMessage);
        when(starboardPostManagementService.findByMessageId(MESSAGE_ID)).thenReturn(Optional.ofNullable(post));
        testUnit.execute(model);
        int callCount = post != null ? 1 : 0;
        verify(starboardPostReactorManagementService, times(callCount)).removeReactors(post);
        verify(starboardService, times(callCount)).deleteStarboardPost(post, null);
    }
}
