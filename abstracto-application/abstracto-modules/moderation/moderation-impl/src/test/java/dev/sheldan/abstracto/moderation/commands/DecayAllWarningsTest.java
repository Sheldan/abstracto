package dev.sheldan.abstracto.moderation.commands;

import dev.sheldan.abstracto.core.command.exception.IncorrectParameterTypeException;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.moderation.service.WarnService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DecayAllWarningsTest {

    @InjectMocks
    private DecayAllWarnings testUnit;

    @Mock
    private WarnService warnService;

    @Test
    public void testDecayAllWarningsWithLog() {
        executeTest(true);
    }

    @Test
    public void testDecayAllWarningsWithoutLog() {
        executeTest(false);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }

    @Test(expected = IncorrectParameterTypeException.class)
    public void testIncorrectParameterType() {
        CommandTestUtilities.executeWrongParametersTestAsync(testUnit);
    }

    private void executeTest(Boolean logWarnings) {
        CommandContext commandContext = CommandTestUtilities.getWithParameters(Arrays.asList(logWarnings));
        when(warnService.decayAllWarningsForServer(commandContext.getUserInitiatedContext().getServer(), logWarnings)).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(commandContext);
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }
}
