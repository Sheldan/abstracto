package dev.sheldan.abstracto.utility.listener.repost;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.listener.GuildMessageEmbedEventModel;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.utility.service.RepostCheckChannelService;
import dev.sheldan.abstracto.utility.service.RepostService;
import dev.sheldan.abstracto.utility.service.management.PostedImageManagement;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.RestAction;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

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
    private BotService botService;

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
        RestAction messageRestAction = Mockito.mock(RestAction.class);
        when(textChannel.retrieveMessageById(MESSAGE_ID)).thenReturn(messageRestAction);
        testUnit.execute(model);
        verify(repostService, times(0)).processMessageEmbedsRepostCheck(anyList(), any(Message.class));
    }

    @Test
    public void testExecuteOneImageEmbed() {
        channelSetup();
        setupMessageHasBeenCovered(false);
        RestAction<Message> messageRestAction = Mockito.mock(RestAction.class);
        MessageEmbed imageEmbed = Mockito.mock(MessageEmbed.class);
        when(imageEmbed.getType()).thenReturn(EmbedType.IMAGE);
        when(model.getEmbeds()).thenReturn(Arrays.asList(imageEmbed));
        mockMessageConsumer(messageRestAction, message);
        when(textChannel.retrieveMessageById(MESSAGE_ID)).thenReturn(messageRestAction);
        testUnit.execute(model);
        verifySingleEmbedProcessed(imageEmbed);
    }

    @Test
    public void testExecuteMultipleEmbedsOneImage() {
        channelSetup();
        setupMessageHasBeenCovered(false);
        RestAction<Message> messageRestAction = Mockito.mock(RestAction.class);
        MessageEmbed imageEmbed = Mockito.mock(MessageEmbed.class);
        MessageEmbed nonImageEmbed = Mockito.mock(MessageEmbed.class);
        when(imageEmbed.getType()).thenReturn(EmbedType.IMAGE);
        when(nonImageEmbed.getType()).thenReturn(EmbedType.LINK);
        when(model.getEmbeds()).thenReturn(Arrays.asList(imageEmbed, nonImageEmbed));
        mockMessageConsumer(messageRestAction, message);
        when(textChannel.retrieveMessageById(MESSAGE_ID)).thenReturn(messageRestAction);
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

    private void mockMessageConsumer(RestAction<Message> action, Message message) {
        doAnswer(invocationOnMock -> {
            Object consumerObj = invocationOnMock.getArguments()[0];
            if(consumerObj instanceof Consumer) {
                Consumer<Message> consumer = (Consumer<Message>) consumerObj;
                consumer.accept(message);
            }
            return null;
        }).when(action).queue(any(Consumer.class));
    }

    private void channelSetup() {
        when(model.getChannelId()).thenReturn(CHANNEL_ID);
        when(model.getServerId()).thenReturn(SERVER_ID);
        when(channelManagementService.loadChannel(CHANNEL_ID)).thenReturn(channel);
        when(botService.getTextChannelFromServer(SERVER_ID, CHANNEL_ID)).thenReturn(textChannel);
        when(repostCheckChannelService.duplicateCheckEnabledForChannel(channel)).thenReturn(true);
    }


}
