package dev.sheldan.abstracto.utility.listener.starboard;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.utility.models.database.StarboardPost;
import dev.sheldan.abstracto.utility.service.management.StarboardPostManagementService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StarboardPostDeletedListenerTest {

    @InjectMocks
    private StarboardPostDeletedListener testUnit;

    @Mock
    private StarboardPostManagementService starboardPostManagementService;

    @Test
    public void deleteNonStarboardPost() {
        Long messageId = 4L;
        when(starboardPostManagementService.findByStarboardPostId(messageId)).thenReturn(Optional.empty());
        CachedMessage cachedMessage = Mockito.mock(CachedMessage.class);
        when(cachedMessage.getMessageId()).thenReturn(messageId);
        testUnit.execute(cachedMessage);
        verify( starboardPostManagementService, times(0)).setStarboardPostIgnored(messageId, true);
    }

    @Test
    public void deleteStarboardPost() {
        Long messageId = 4L;
        Long postMessageId = 5L;
        Long serverId = 3L;
        AChannel sourceChannel = Mockito.mock(AChannel.class);
        StarboardPost post = Mockito.mock(StarboardPost.class);
        when(post.getSourceChannel()) .thenReturn(sourceChannel);
        when(post.getPostMessageId()).thenReturn(postMessageId);
        when(starboardPostManagementService.findByStarboardPostId(messageId)).thenReturn(Optional.of(post));
        CachedMessage cachedMessage = Mockito.mock(CachedMessage.class);
        when(cachedMessage.getServerId()).thenReturn(serverId);
        when(cachedMessage.getMessageId()).thenReturn(messageId);
        testUnit.execute(cachedMessage);
        verify( starboardPostManagementService, times(1)).setStarboardPostIgnored(messageId, true);
    }

}
