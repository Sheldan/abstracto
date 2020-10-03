package dev.sheldan.abstracto.core.command.handler;

import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
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
public class EmoteParameterHandlerTest {

    @InjectMocks
    private EmoteParameterHandler testUnit;

    @Mock
    private CommandParameterIterators iterators;

    @Mock
    private Emote emote;

    @Mock
    private Message message;

    @Mock
    private Guild guild;

    private static final Long EMOTE_ID = 111111111111111111L;
    private static final String EMOTE_NAME = "test";

    @Test
    public void testSuccessfulCondition() {
        Assert.assertTrue(testUnit.handles(Emote.class));
    }

    @Test
    public void testWrongCondition() {
        Assert.assertFalse(testUnit.handles(String.class));
    }

    @Test
    public void testProperEmoteMention() {
        oneEmoteInIterator();
        String input = getEmoteMention();
        Emote parsed = (Emote) testUnit.handle(input, iterators, Emote.class, null);
        Assert.assertEquals(parsed, emote);
    }

    @Test
    public void testEmoteById() {
        setupMessage();
        String input = EMOTE_ID.toString();
        Emote parsed = (Emote) testUnit.handle(input, null, Emote.class, message);
        Assert.assertEquals(parsed, emote);
    }

    @Test
    public void testInvalidEmoteMention() {
        Assert.assertNull(testUnit.handle("test", null, Emote.class, null));
    }

    private String getEmoteMention() {
        return String.format("<:%s:%d>", EMOTE_NAME, EMOTE_ID);
    }

    private void oneEmoteInIterator() {
        List<Emote> emotes = Arrays.asList(emote);
        when(iterators.getEmoteIterator()).thenReturn(emotes.iterator());
    }

    private void setupMessage()  {
        when(message.getGuild()).thenReturn(guild);
        when(guild.getEmoteById(EMOTE_ID)).thenReturn(emote);
    }

}
