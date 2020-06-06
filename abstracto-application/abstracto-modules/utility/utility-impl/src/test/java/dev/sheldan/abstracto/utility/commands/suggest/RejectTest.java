package dev.sheldan.abstracto.utility.commands.suggest;

import dev.sheldan.abstracto.core.command.exception.IncorrectParameter;
import dev.sheldan.abstracto.core.command.exception.InsufficientParameters;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.utility.models.template.commands.SuggestionLog;
import dev.sheldan.abstracto.utility.service.SuggestionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RejectTest {

    @InjectMocks
    private Reject testUnit;

    @Mock
    private SuggestionService suggestionService;

    @Test(expected = InsufficientParameters.class)
    public void testTooLittleParameters() {
        CommandTestUtilities.executeNoParametersTest(testUnit);
    }

    @Test(expected = IncorrectParameter.class)
    public void testIncorrectParameterType() {
        CommandTestUtilities.executeWrongParametersTest(testUnit);
    }

    @Test
    public void testExecuteCommand() {
        String text = "text";
        Long suggestionId = 5L;
        CommandContext context = CommandTestUtilities.getWithParameters(Arrays.asList(suggestionId, text));
        CommandResult result = testUnit.execute(context);
        verify(suggestionService, times(1)).rejectSuggestion(eq(suggestionId), eq(text), any(SuggestionLog.class));
        CommandTestUtilities.checkSuccessfulCompletion(result);
    }
}
