package dev.sheldan.abstracto.utility.commands;

import dev.sheldan.abstracto.core.command.exception.IncorrectParameter;
import dev.sheldan.abstracto.core.command.exception.InsufficientParameters;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.utility.models.template.commands.ShowEmoteLog;
import net.dv8tion.jda.api.entities.Emote;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ShowEmoteTest {

    @InjectMocks
    private ShowEmote testUnit;

    @Mock
    private ChannelService channelService;

    @Captor
    private ArgumentCaptor<ShowEmoteLog> emoteLogArgumentCaptor;

    @Test(expected = IncorrectParameter.class)
    public void testIncorrectParameterType() {
        CommandTestUtilities.executeWrongParametersTest(testUnit);
    }

    @Test(expected = InsufficientParameters.class)
    public void testTooLittleParameters() {
        CommandTestUtilities.executeNoParametersTest(testUnit);
    }

    @Test
    public void executeCommandWithOneEmote() {
        Emote emote = Mockito.mock(Emote.class);
        CommandContext noParameters = CommandTestUtilities.getWithParameters(Arrays.asList(emote));
        CommandResult result = testUnit.execute(noParameters);
        verify(channelService, times(1)).sendEmbedTemplateInChannel(eq(ShowEmote.SHOW_EMOTE_RESPONSE_TEMPLATE), emoteLogArgumentCaptor.capture(), eq(noParameters.getChannel()));
        CommandTestUtilities.checkSuccessfulCompletion(result);
        ShowEmoteLog usedLog = emoteLogArgumentCaptor.getValue();
        Assert.assertEquals(emote, usedLog.getEmote());
    }

    @Test
    public void executeCommandWithTwoEmotes() {
        Emote emote = Mockito.mock(Emote.class);
        Emote secondEmote = Mockito.mock(Emote.class);
        CommandContext noParameters = CommandTestUtilities.getWithParameters(Arrays.asList(emote, secondEmote));
        CommandResult result = testUnit.execute(noParameters);
        verify(channelService, times(1)).sendEmbedTemplateInChannel(eq(ShowEmote.SHOW_EMOTE_RESPONSE_TEMPLATE), emoteLogArgumentCaptor.capture(), eq(noParameters.getChannel()));
        CommandTestUtilities.checkSuccessfulCompletion(result);
        ShowEmoteLog usedLog = emoteLogArgumentCaptor.getValue();
        Assert.assertEquals(emote, usedLog.getEmote());
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }

}
