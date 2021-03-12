package dev.sheldan.abstracto.entertainment.command;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.entertainment.model.RouletteResponseModel;
import dev.sheldan.abstracto.entertainment.service.EntertainmentService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RouletteTest {

    @InjectMocks
    private Roulette testUnit;

    @Mock
    private EntertainmentService entertainmentService;

    @Mock
    private ChannelService channelService;

    @Captor
    private ArgumentCaptor<RouletteResponseModel> responseModelArgumentCaptor;

    @Test
    public void executeWithNoParameter() {
        CommandContext noParameters = CommandTestUtilities.getNoParameters();
        Boolean result = false;
        when(entertainmentService.executeRoulette(noParameters.getAuthor())).thenReturn(result);
        when(channelService.sendEmbedTemplateInTextChannelList(eq(Roulette.ROULETTE_RESPONSE_TEMPLATE_KEY), responseModelArgumentCaptor.capture(), eq(noParameters.getChannel()))).thenReturn(CommandTestUtilities.messageFutureList());
        CompletableFuture<CommandResult> futureResult = testUnit.executeAsync(noParameters);
        CommandTestUtilities.checkSuccessfulCompletionAsync(futureResult);
        Assert.assertEquals(result, responseModelArgumentCaptor.getValue().getResult());
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }

}
