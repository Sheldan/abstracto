package dev.sheldan.abstracto.utility.listener.repost;

import dev.sheldan.abstracto.core.models.cache.CachedEmbed;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.utility.service.RepostCheckChannelService;
import dev.sheldan.abstracto.utility.service.RepostService;
import net.dv8tion.jda.api.entities.EmbedType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RepostMessageReceivedListenerTest {

    @InjectMocks
    private RepostMessageReceivedListener testUnit;

    @Mock
    private RepostCheckChannelService repostCheckChannelService;

    @Mock
    private ChannelManagementService channelManagementService;

    @Mock
    private RepostService repostService;

    @Mock
    private CachedMessage message;

    @Mock
    private AChannel channel;

    @Captor
    private ArgumentCaptor<List<CachedEmbed>> embedListCaptor;

    private static final Long CHANNEL_ID = 4L;

    @Test
    public void testExecuteCheckDisabled() {
        setupRepostCheckEnabled(false);
        testUnit.execute(message);
        verify(repostService, times(0)).processMessageAttachmentRepostCheck(message);
    }

    @Test
    public void testExecuteOnlyMessage() {
        setupRepostCheckEnabled(true);
        testUnit.execute(message);
        verify(repostService, times(1)).processMessageAttachmentRepostCheck(message);
        verify(repostService, times(1)).processMessageEmbedsRepostCheck(embedListCaptor.capture(), eq(message));
        Assert.assertEquals(0, embedListCaptor.getValue().size());
    }

    @Test
    public void testExecuteOnlyMessageOneImageAttachment() {
        setupRepostCheckEnabled(true);
        CachedEmbed imageEmbed = Mockito.mock(CachedEmbed.class);
        when(imageEmbed.getType()).thenReturn(EmbedType.IMAGE);
        when(message.getEmbeds()).thenReturn(Arrays.asList(imageEmbed));
        testUnit.execute(message);
        verifySingleEmbed(imageEmbed);
    }

    @Test
    public void testExecuteOnlyMessageTwoEmbedsOneImageAttachment() {
        setupRepostCheckEnabled(true);
        CachedEmbed imageEmbed = Mockito.mock(CachedEmbed.class);
        when(imageEmbed.getType()).thenReturn(EmbedType.IMAGE);
        CachedEmbed nonImageEmbed = Mockito.mock(CachedEmbed.class);
        when(nonImageEmbed.getType()).thenReturn(EmbedType.LINK);
        when(message.getEmbeds()).thenReturn(Arrays.asList(imageEmbed, nonImageEmbed));
        testUnit.execute(message);
        verifySingleEmbed(imageEmbed);
    }

    private void setupRepostCheckEnabled(boolean b) {
        when(message.getChannelId()).thenReturn(CHANNEL_ID);
        when(channelManagementService.loadChannel(CHANNEL_ID)).thenReturn(channel);
        when(repostCheckChannelService.duplicateCheckEnabledForChannel(channel)).thenReturn(b);
    }

    private void verifySingleEmbed(CachedEmbed imageEmbed) {
        verify(repostService, times(1)).processMessageAttachmentRepostCheck(message);
        verify(repostService, times(1)).processMessageEmbedsRepostCheck(embedListCaptor.capture(), eq(message));
        List<CachedEmbed> processedEmbeds = embedListCaptor.getValue();
        Assert.assertEquals(1, processedEmbeds.size());
        Assert.assertEquals(imageEmbed, processedEmbeds.get(0));
    }

}
