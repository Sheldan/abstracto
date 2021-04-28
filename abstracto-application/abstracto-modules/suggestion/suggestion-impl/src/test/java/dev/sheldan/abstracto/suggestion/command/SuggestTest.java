package dev.sheldan.abstracto.suggestion.command;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.suggestion.model.template.SuggestionLog;
import dev.sheldan.abstracto.suggestion.service.SuggestionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SuggestTest {

    @InjectMocks
    private Suggest testUnit;

    @Mock
    private SuggestionService suggestionService;

    @Test
    public void testExecuteCommand() throws ExecutionException, InterruptedException {
        String text = "text";
        CommandContext context = CommandTestUtilities.getWithParameters(Arrays.asList(text));
        when(suggestionService.createSuggestionMessage(eq(context.getMessage()), eq(text))).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(context);
        verify(suggestionService, times(1)).createSuggestionMessage(eq(context.getMessage()), eq(text));
        CommandTestUtilities.checkSuccessfulCompletion(result.get());
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }

}
