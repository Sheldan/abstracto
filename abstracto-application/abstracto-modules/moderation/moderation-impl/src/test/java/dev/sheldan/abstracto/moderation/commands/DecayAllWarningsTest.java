package dev.sheldan.abstracto.moderation.commands;

import dev.sheldan.abstracto.core.command.exception.IncorrectParameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.moderation.service.WarnService;
import dev.sheldan.abstracto.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.test.command.CommandTestUtilities;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

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

    @Test(expected = IncorrectParameter.class)
    public void testIncorrectParameterType() {
        CommandTestUtilities.executeWrongParametersTest(testUnit);
    }

    private void executeTest(Boolean logWarnings) {
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(logWarnings));
        CommandResult result = testUnit.execute(parameters);
        verify(warnService, times(1)).decayAllWarningsForServer(parameters.getUserInitiatedContext().getServer(), logWarnings);
        CommandTestUtilities.checkSuccessfulCompletion(result);
    }
}
