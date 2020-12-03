package dev.sheldan.abstracto.utility.listener.repost;

import dev.sheldan.abstracto.utility.service.RepostCheckChannelService;
import dev.sheldan.abstracto.utility.service.RepostService;
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

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RepostMessageReceivedListenerTest {

    @InjectMocks
    private RepostMessageReceivedListener testUnit;

    @Mock
    private RepostCheckChannelService repostCheckChannelService;

    @Mock
    private RepostService repostService;

    @Mock
    private Message message;

    @Mock
    private TextChannel textChannel;

    @Captor
    private ArgumentCaptor<List<MessageEmbed>> embedListCaptor;

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
        MessageEmbed imageEmbed = Mockito.mock(MessageEmbed.class);
        when(imageEmbed.getType()).thenReturn(EmbedType.IMAGE);
        when(message.getEmbeds()).thenReturn(Arrays.asList(imageEmbed));
        testUnit.execute(message);
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
        testUnit.execute(message);
        verifySingleEmbed(imageEmbed);
    }

    private void setupRepostCheckEnabled(boolean b) {
        when(message.getTextChannel()).thenReturn(textChannel);
        when(repostCheckChannelService.duplicateCheckEnabledForChannel(textChannel)).thenReturn(b);
    }

    private void verifySingleEmbed(MessageEmbed imageEmbed) {
        verify(repostService, times(1)).processMessageAttachmentRepostCheck(message);
        verify(repostService, times(1)).processMessageEmbedsRepostCheck(embedListCaptor.capture(), eq(message));
        List<MessageEmbed> processedEmbeds = embedListCaptor.getValue();
        Assert.assertEquals(1, processedEmbeds.size());
        Assert.assertEquals(imageEmbed, processedEmbeds.get(0));
    }

}
