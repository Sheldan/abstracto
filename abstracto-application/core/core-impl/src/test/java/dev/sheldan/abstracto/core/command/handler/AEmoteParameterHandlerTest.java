package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.service.EmoteService;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AEmoteParameterHandlerTest {

    @InjectMocks
    private AEmoteParameterHandler testUnit;

    @Mock
    private EmoteParameterHandler emoteParameterHandler;

    @Mock
    private EmoteService emoteService;

    @Mock
    private CommandParameterIterators iterators;

    @Mock
    private Emote emote;

    @Mock
    private Message message;

    @Mock
    private AEmote aEmote;

    @Test
    public void testSuccessfulCondition() {
        Assert.assertTrue(testUnit.handles(AEmote.class));
    }

    @Test
    public void testWrongCondition() {
        Assert.assertFalse(testUnit.handles(String.class));
    }

    @Test
    public void testProperEmoteMention() {
        String input = "test";
        when(emoteParameterHandler.handle(input, iterators, Emote.class, message)).thenReturn(emote);
        when(emoteService.getFakeEmoteFromEmote(emote)).thenReturn(aEmote);
        AEmote parsed = (AEmote) testUnit.handle(input, iterators, AEmote.class, message);
        Assert.assertEquals(aEmote, parsed);
    }


}
