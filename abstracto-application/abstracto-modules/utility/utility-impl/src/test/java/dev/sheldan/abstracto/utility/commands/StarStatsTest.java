package dev.sheldan.abstracto.utility.commands;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.utility.models.template.commands.starboard.StarStatsModel;
import dev.sheldan.abstracto.utility.service.StarboardService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StarStatsTest {

    @InjectMocks
    private StarStats testUnit;

    @Mock
    private StarboardService starboardService;

    @Mock
    private ChannelService channelService;

    @Test
    public void executeCommand() {
        CommandContext noParameters = CommandTestUtilities.getNoParameters();
        when(noParameters.getGuild().getIdLong()).thenReturn(noParameters.getUserInitiatedContext().getChannel().getId());
        StarStatsModel starStatsModel = StarStatsModel.builder().build();
        when(starboardService.retrieveStarStats(noParameters.getGuild().getIdLong())).thenReturn(starStatsModel);
        CompletableFuture<CommandResult> result = testUnit.executeAsync(noParameters);
        verify(channelService, times(1)).sendEmbedTemplateInChannel(StarStats.STARSTATS_RESPONSE_TEMPLATE, starStatsModel, noParameters.getChannel());
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }

}
