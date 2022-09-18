package dev.sheldan.abstracto.repostdetection.listener;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.listener.MessageReceivedModel;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.repostdetection.service.RepostCheckChannelService;
import dev.sheldan.abstracto.repostdetection.service.RepostService;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
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
    private Message message;

    @Mock
    private AChannel channel;

    @Mock
    private MessageReceivedModel model;

    @Mock
    private MessageChannelUnion textChannel;

    @Captor
    private ArgumentCaptor<List<MessageEmbed>> embedListCaptor;

    private static final Long CHANNEL_ID = 4L;

    @Test
    public void testExecuteCheckDisabled() {
        setupRepostCheckEnabled(false);
        testUnit.execute(model);
        verify(repostService, times(0)).processMessageAttachmentRepostCheck(message);
    }

    @Test
    public void testExecuteOnlyMessage() {
        setupRepostCheckEnabled(true);
        testUnit.execute(model);
        verify(repostService, times(1)).processMessageAttachmentRepostCheck(message);
        verify(repostService, times(1)).processMessageEmbedsRepostCheck(embedListCaptor.capture(), eq(message));
        Assert.assertEquals(0, embedListCaptor.getValue().size());
    }

    @Test
    public void testExecuteOnlyMessageOneImageAttachment() {
        setupRepostCheckEnabled(true);
        MessageEmbed imageEmbed = Mockito.mock(MessageEmbed.class);
        when(imageEmbed.getType()).thenReturn(EmbedType.IMAGE);
        when(message.getEmbeds()).thenReturn(Arrays.asList(imageEmbed));
        testUnit.execute(model);
        verifySingleEmbed(imageEmbed);
    }

    @Test
    public void testExecuteOnlyMessageTwoEmbedsOneImageAttachment() {
        setupRepostCheckEnabled(true);
        MessageEmbed imageEmbed = Mockito.mock(MessageEmbed.class);
        when(imageEmbed.getType()).thenReturn(EmbedType.IMAGE);
        MessageEmbed nonImageEmbed = Mockito.mock(MessageEmbed.class);
        when(nonImageEmbed.getType()).thenReturn(EmbedType.LINK);
        when(message.getEmbeds()).thenReturn(Arrays.asList(imageEmbed, nonImageEmbed));
        when(model.getMessage()).thenReturn(message);
        testUnit.execute(model);
        verifySingleEmbed(imageEmbed);
    }

    private void setupRepostCheckEnabled(boolean b) {
        when(message.getChannel()).thenReturn(textChannel);
        when(message.isFromGuild()).thenReturn(true);
        when(message.isWebhookMessage()).thenReturn(false);
        MessageType type = MessageType.DEFAULT;
        when(message.getType()).thenReturn(type);
        when(model.getMessage()).thenReturn(message);
        when(textChannel.getIdLong()).thenReturn(CHANNEL_ID);
        when(channelManagementService.loadChannel(CHANNEL_ID)).thenReturn(channel);
        when(repostCheckChannelService.duplicateCheckEnabledForChannel(channel)).thenReturn(b);
    }

    private void verifySingleEmbed(MessageEmbed imageEmbed) {
        verify(repostService, times(1)).processMessageAttachmentRepostCheck(message);
        verify(repostService, times(1)).processMessageEmbedsRepostCheck(embedListCaptor.capture(), eq(message));
        List<MessageEmbed> processedEmbeds = embedListCaptor.getValue();
        Assert.assertEquals(1, processedEmbeds.size());
        Assert.assertEquals(imageEmbed, processedEmbeds.get(0));
    }

}
