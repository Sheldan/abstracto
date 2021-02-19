package dev.sheldan.abstracto.utility.commands;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.utility.models.template.commands.ShowEmoteLog;
import net.dv8tion.jda.api.entities.Emote;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ShowEmoteTest {

    @InjectMocks
    private ShowEmote testUnit;

    @Mock
    private ChannelService channelService;

    @Captor
    private ArgumentCaptor<ShowEmoteLog> emoteLogArgumentCaptor;

    @Test
    public void executeCommandWithOneEmote() {
        Emote emote = Mockito.mock(Emote.class);
        CommandContext noParameters = CommandTestUtilities.getWithParameters(Arrays.asList(emote));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(noParameters);
        verify(channelService, times(1)).sendEmbedTemplateInTextChannelList(eq(ShowEmote.SHOW_EMOTE_RESPONSE_TEMPLATE), emoteLogArgumentCaptor.capture(), eq(noParameters.getChannel()));
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
        ShowEmoteLog usedLog = emoteLogArgumentCaptor.getValue();
        Assert.assertEquals(emote, usedLog.getEmote());
    }

    @Test
    public void executeCommandWithTwoEmotes() {
        Emote emote = Mockito.mock(Emote.class);
        Emote secondEmote = Mockito.mock(Emote.class);
        CommandContext noParameters = CommandTestUtilities.getWithParameters(Arrays.asList(emote, secondEmote));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(noParameters);
        verify(channelService, times(1)).sendEmbedTemplateInTextChannelList(eq(ShowEmote.SHOW_EMOTE_RESPONSE_TEMPLATE), emoteLogArgumentCaptor.capture(), eq(noParameters.getChannel()));
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
        ShowEmoteLog usedLog = emoteLogArgumentCaptor.getValue();
        Assert.assertEquals(emote, usedLog.getEmote());
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }

}
