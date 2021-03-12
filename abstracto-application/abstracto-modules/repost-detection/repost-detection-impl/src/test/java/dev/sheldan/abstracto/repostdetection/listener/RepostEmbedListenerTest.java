package dev.sheldan.abstracto.repostdetection.listener;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.listener.GuildMessageEmbedEventModel;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.repostdetection.service.RepostCheckChannelService;
import dev.sheldan.abstracto.repostdetection.service.RepostService;
import dev.sheldan.abstracto.repostdetection.service.management.PostedImageManagement;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RepostEmbedListenerTest {

    @InjectMocks
    private RepostEmbedListener testUnit;

    @Mock
    private RepostCheckChannelService repostCheckChannelService;

    @Mock
    private RepostService repostService;

    @Mock
    private PostedImageManagement repostManagement;

    @Mock
    private ChannelManagementService channelManagementService;

    @Mock
    private ChannelService channelService;

    @Mock
    private GuildMessageEmbedEventModel model;

    @Mock
    private TextChannel textChannel;

    @Mock
    private Message message;

    @Mock
    private AChannel channel;

    @Captor
    private ArgumentCaptor<List<MessageEmbed>> embedListsParameterCaptor;

    private static final Long MESSAGE_ID = 1L;
    private static final Long CHANNEL_ID = 2L;
    private static final Long SERVER_ID = 3L;

    @Test
    public void testExecuteCheckDisabled() {
        when(model.getChannelId()).thenReturn(CHANNEL_ID);
        testUnit.execute(model);
        verify(repostManagement, times(0)).messageEmbedsHaveBeenCovered(anyLong());
    }

    @Test
    public void testExecuteEmbedsHaveBeenCovered() {
        channelSetup();
        setupMessageHasBeenCovered(true);
        testUnit.execute(model);
        verify(repostService, times(0)).processMessageEmbedsRepostCheck(anyList(), any(Message.class));
    }

    @Test
    public void testExecuteNoEmbeds() {
        channelSetup();
        setupMessageHasBeenCovered(false);
        when(channelService.retrieveMessageInChannel(SERVER_ID, CHANNEL_ID, MESSAGE_ID)).thenReturn(CompletableFuture.completedFuture(message));
        testUnit.execute(model);
        verify(repostService, times(0)).processMessageEmbedsRepostCheck(anyList(), any(Message.class));
    }

    @Test
    public void testExecuteOneImageEmbed() {
        channelSetup();
        setupMessageHasBeenCovered(false);
        MessageEmbed imageEmbed = Mockito.mock(MessageEmbed.class);
        when(imageEmbed.getType()).thenReturn(EmbedType.IMAGE);
        when(model.getEmbeds()).thenReturn(Arrays.asList(imageEmbed));
        when(channelService.retrieveMessageInChannel(SERVER_ID, CHANNEL_ID, MESSAGE_ID)).thenReturn(CompletableFuture.completedFuture(message));
        testUnit.execute(model);
        verifySingleEmbedProcessed(imageEmbed);
    }

    @Test
    public void testExecuteMultipleEmbedsOneImage() {
        channelSetup();
        setupMessageHasBeenCovered(false);
        MessageEmbed imageEmbed = Mockito.mock(MessageEmbed.class);
        MessageEmbed nonImageEmbed = Mockito.mock(MessageEmbed.class);
        when(imageEmbed.getType()).thenReturn(EmbedType.IMAGE);
        when(nonImageEmbed.getType()).thenReturn(EmbedType.LINK);
        when(model.getEmbeds()).thenReturn(Arrays.asList(imageEmbed, nonImageEmbed));
        when(channelService.retrieveMessageInChannel(SERVER_ID, CHANNEL_ID, MESSAGE_ID)).thenReturn(CompletableFuture.completedFuture(message));
        testUnit.execute(model);
        verifySingleEmbedProcessed(imageEmbed);
    }

    private void setupMessageHasBeenCovered(boolean covered) {
        when(model.getMessageId()).thenReturn(MESSAGE_ID);
        when(repostManagement.messageEmbedsHaveBeenCovered(MESSAGE_ID)).thenReturn(covered);
    }

    private void verifySingleEmbedProcessed(MessageEmbed imageEmbed) {
        verify(repostService, times(1)).processMessageEmbedsRepostCheck(embedListsParameterCaptor.capture(), eq(message));
        List<MessageEmbed> embeds = embedListsParameterCaptor.getValue();
        Assert.assertEquals(1, embeds.size());
        Assert.assertEquals(imageEmbed, embeds.get(0));
    }


    private void channelSetup() {
        when(model.getChannelId()).thenReturn(CHANNEL_ID);
        when(model.getServerId()).thenReturn(SERVER_ID);
        when(channelManagementService.loadChannel(CHANNEL_ID)).thenReturn(channel);
        when(repostCheckChannelService.duplicateCheckEnabledForChannel(channel)).thenReturn(true);
    }


}
