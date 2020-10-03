package dev.sheldan.abstracto.core.command.handler;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TextChannelParameterHandlerTest {

    @InjectMocks
    private TextChannelParameterHandler testUnit;

    @Mock
    private CommandParameterIterators iterators;

    @Mock
    private TextChannel channel;

    @Mock
    private Message message;

    @Mock
    private Guild guild;

    private static final Long CHANNEL_ID = 111111111111111111L;

    @Test
    public void testSuccessfulCondition() {
        Assert.assertTrue(testUnit.handles(TextChannel.class));
    }

    @Test
    public void testWrongCondition() {
        Assert.assertFalse(testUnit.handles(String.class));
    }

    @Test
    public void testProperChannelMention() {
        oneChannelInIterator();
        String input = getChannelMention();
        TextChannel parsed = (TextChannel) testUnit.handle(input, iterators, TextChannel.class, null);
        Assert.assertEquals(parsed, channel);
    }

    @Test
    public void testChannelMentionById() {
        setupMessage();
        String input = CHANNEL_ID.toString();
        TextChannel parsed = (TextChannel) testUnit.handle(input, null, TextChannel.class, message);
        Assert.assertEquals(parsed, channel);
    }

    @Test(expected = NumberFormatException.class)
    public void testInvalidChannelMention() {
        String input = "test";
        testUnit.handle(input, null, TextChannel.class, null);
    }

    private String getChannelMention() {
        return String.format("<#%d>", CHANNEL_ID);
    }

    private void oneChannelInIterator() {
        List<TextChannel> channels = Arrays.asList(channel);
        when(iterators.getChannelIterator()).thenReturn(channels.iterator());
    }

    private void setupMessage()  {
        when(message.getGuild()).thenReturn(guild);
        when(guild.getTextChannelById(CHANNEL_ID)).thenReturn(channel);
    }


}
