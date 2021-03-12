package dev.sheldan.abstracto.entertainment.command;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.entertainment.model.LoveCalcResponseModel;
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

import static dev.sheldan.abstracto.entertainment.command.LoveCalc.LOVE_CALC_RESPONSE_TEMPLATE_KEY;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LoveCalcTest {

    @InjectMocks
    private LoveCalc testUnit;

    @Mock
    private EntertainmentService entertainmentService;

    @Mock
    private ChannelService channelService;

    @Captor
    private ArgumentCaptor<LoveCalcResponseModel> responseModelArgumentCaptor;

    @Test
    public void execute8BallCommand() {
        String inputText = "text";
        String inputText2 = "text2";
        Integer loveResult = 2;
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(inputText, inputText2));
        when(entertainmentService.getLoveCalcValue(inputText, inputText2)).thenReturn(loveResult);
        when(channelService.sendEmbedTemplateInTextChannelList(eq(LOVE_CALC_RESPONSE_TEMPLATE_KEY), responseModelArgumentCaptor.capture(), eq(parameters.getChannel()))).thenReturn(CommandTestUtilities.messageFutureList());
        CompletableFuture<CommandResult> result = testUnit.executeAsync(parameters);
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
        Assert.assertEquals(loveResult, responseModelArgumentCaptor.getValue().getRolled());
        Assert.assertEquals(inputText, responseModelArgumentCaptor.getValue().getFirstPart());
        Assert.assertEquals(inputText2, responseModelArgumentCaptor.getValue().getSecondPart());
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }

}
