package dev.sheldan.abstracto.entertainment.command;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.entertainment.model.command.EightBallResponseModel;
import dev.sheldan.abstracto.entertainment.service.EntertainmentService;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EightBallTest {

    @InjectMocks
    private EightBall testUnit;

    @Mock
    private EntertainmentService entertainmentService;

    @Mock
    private ChannelService channelService;

    @Captor
    private ArgumentCaptor<EightBallResponseModel> responseModelArgumentCaptor;

    @Test
    public void execute8BallCommand() {
        String inputText = "text";
        String chosenKey = "key";
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(inputText));
        when(entertainmentService.getEightBallValue(inputText)).thenReturn(chosenKey);
        when(channelService.sendEmbedTemplateInTextChannelList(eq(EightBall.EIGHT_BALL_RESPONSE_TEMPLATE_KEY), responseModelArgumentCaptor.capture(), eq(parameters.getChannel()))).thenReturn(CommandTestUtilities.messageFutureList());
        CompletableFuture<CommandResult> result = testUnit.executeAsync(parameters);
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
        Assert.assertEquals(chosenKey, responseModelArgumentCaptor.getValue().getChosenKey());
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }

}
