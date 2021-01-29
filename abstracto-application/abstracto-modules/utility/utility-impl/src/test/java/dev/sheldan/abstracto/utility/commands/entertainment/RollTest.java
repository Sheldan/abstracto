package dev.sheldan.abstracto.utility.commands.entertainment;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.utility.models.template.commands.RollResponseModel;
import dev.sheldan.abstracto.utility.service.EntertainmentService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static dev.sheldan.abstracto.utility.commands.entertainment.Roll.ROLL_RESPONSE_TEMPLATE_KEY;
import static dev.sheldan.abstracto.utility.config.features.EntertainmentFeature.ROLL_DEFAULT_HIGH_KEY;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RollTest {

    @InjectMocks
    private Roll testUnit;

    @Mock
    private EntertainmentService entertainmentService;

    @Mock
    private ChannelService channelService;

    @Mock
    private ConfigService configService;

    @Captor
    private ArgumentCaptor<RollResponseModel> responseModelArgumentCaptor;

    @Test
    public void executeWithNoParameter() {
        CommandContext noParameters = CommandTestUtilities.getNoParameters();
        Integer result = 4;
        Long serverId = 3L;
        Integer max = 10;
        when(noParameters.getGuild().getIdLong()).thenReturn(serverId);
        when(configService.getLongValue(ROLL_DEFAULT_HIGH_KEY, serverId)).thenReturn(max.longValue());
        when(entertainmentService.calculateRollResult(1, max)).thenReturn(result);
        when(channelService.sendEmbedTemplateInChannel(eq(ROLL_RESPONSE_TEMPLATE_KEY), responseModelArgumentCaptor.capture(), eq(noParameters.getChannel()))).thenReturn(CommandTestUtilities.messageFutureList());
        CompletableFuture<CommandResult> futureResult = testUnit.executeAsync(noParameters);
        CommandTestUtilities.checkSuccessfulCompletionAsync(futureResult);
        Assert.assertEquals(4, responseModelArgumentCaptor.getValue().getRolled().intValue());
    }

    @Test
    public void executeWithHighParameter() {
        CommandContext noParameters = CommandTestUtilities.getWithParameters(Arrays.asList(20));
        Integer result = 4;
        when(entertainmentService.calculateRollResult(1, 20)).thenReturn(result);
        when(channelService.sendEmbedTemplateInChannel(eq(ROLL_RESPONSE_TEMPLATE_KEY), responseModelArgumentCaptor.capture(), eq(noParameters.getChannel()))).thenReturn(CommandTestUtilities.messageFutureList());
        CompletableFuture<CommandResult> futureResult = testUnit.executeAsync(noParameters);
        CommandTestUtilities.checkSuccessfulCompletionAsync(futureResult);
        Assert.assertEquals(4, responseModelArgumentCaptor.getValue().getRolled().intValue());
    }

    @Test
    public void executeWithBothParameters() {
        CommandContext noParameters = CommandTestUtilities.getWithParameters(Arrays.asList(20, 10));
        Integer result = 4;
        when(entertainmentService.calculateRollResult(10, 20)).thenReturn(result);
        when(channelService.sendEmbedTemplateInChannel(eq(ROLL_RESPONSE_TEMPLATE_KEY), responseModelArgumentCaptor.capture(), eq(noParameters.getChannel()))).thenReturn(CommandTestUtilities.messageFutureList());
        CompletableFuture<CommandResult> futureResult = testUnit.executeAsync(noParameters);
        CommandTestUtilities.checkSuccessfulCompletionAsync(futureResult);
        Assert.assertEquals(4, responseModelArgumentCaptor.getValue().getRolled().intValue());
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }

}
