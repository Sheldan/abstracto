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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RejectTest {

    @InjectMocks
    private Reject testUnit;

    @Mock
    private SuggestionService suggestionService;

    @Test
    public void testExecuteCommand() throws ExecutionException, InterruptedException {
        String text = "text";
        Long suggestionId = 5L;
        CommandContext context = CommandTestUtilities.getWithParameters(Arrays.asList(suggestionId, text));
        when(suggestionService.rejectSuggestion(eq(suggestionId), eq(context.getMessage()), eq(text))).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(context);
        verify(suggestionService, times(1)).rejectSuggestion(eq(suggestionId), eq(context.getMessage()), eq(text));
        CommandTestUtilities.checkSuccessfulCompletion(result.get());
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
