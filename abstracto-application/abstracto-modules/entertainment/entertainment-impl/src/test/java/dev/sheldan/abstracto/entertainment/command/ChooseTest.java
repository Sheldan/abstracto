package dev.sheldan.abstracto.entertainment.command;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.entertainment.model.ChooseResponseModel;
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
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChooseTest {

    @InjectMocks
    private Choose testUnit;

    @Mock
    private EntertainmentService entertainmentService;

    @Mock
    private ChannelService channelService;

    @Captor
    private ArgumentCaptor<ChooseResponseModel> responseModelArgumentCaptor;

    @Test
    public void executeChooseCommand() {
        List<String> choices = Arrays.asList("choice1", "choice2");
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(choices));
        when(entertainmentService.takeChoice(choices, parameters.getAuthor())).thenReturn(choices.get(0));
        when(channelService.sendEmbedTemplateInTextChannelList(eq(Choose.CHOOSE_RESPONSE_TEMPLATE_KEY), responseModelArgumentCaptor.capture(), eq(parameters.getChannel()))).thenReturn(CommandTestUtilities.messageFutureList());
        CompletableFuture<CommandResult> result = testUnit.executeAsync(parameters);
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
        Assert.assertEquals(choices.get(0), responseModelArgumentCaptor.getValue().getChosenValue());
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
