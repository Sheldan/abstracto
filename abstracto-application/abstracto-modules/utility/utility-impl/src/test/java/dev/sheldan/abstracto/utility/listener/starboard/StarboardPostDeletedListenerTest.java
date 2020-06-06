package dev.sheldan.abstracto.utility.listener.starboard;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.test.MockUtils;
import dev.sheldan.abstracto.utility.models.database.StarboardPost;
import dev.sheldan.abstracto.utility.service.management.StarboardPostManagementService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
        CachedMessage cachedMessage = CachedMessage
                .builder()
                .messageId(messageId)
                .build();
        testUnit.execute(cachedMessage);
        verify( starboardPostManagementService, times(0)).setStarboardPostIgnored(messageId, true);
    }

    @Test
    public void deleteStarboardPost() {
        Long messageId = 4L;
        AServer server = MockUtils.getServer();
        AUserInAServer author = MockUtils.getUserObject(4L, server);
        AChannel sourceChannel = MockUtils.getTextChannel(server, 6L);
        StarboardPost post = StarboardPost.builder().author(author).postMessageId(5L).sourceChanel(sourceChannel).build();
        when(starboardPostManagementService.findByStarboardPostId(messageId)).thenReturn(Optional.of(post));
        CachedMessage cachedMessage = CachedMessage
                .builder()
                .messageId(messageId)
                .build();
        testUnit.execute(cachedMessage);
        verify( starboardPostManagementService, times(1)).setStarboardPostIgnored(messageId, true);
    }

}
