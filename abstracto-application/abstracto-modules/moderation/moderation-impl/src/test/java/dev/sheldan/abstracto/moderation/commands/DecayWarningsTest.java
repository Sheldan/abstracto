package dev.sheldan.abstracto.moderation.commands;

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

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DecayWarningsTest {

    @InjectMocks
    private DecayWarnings testUnit;

    @Mock
    private WarnService warnService;

    @Test
    public void testExecuteCommand() {
        CommandContext noParameters = CommandTestUtilities.getNoParameters();
        CommandResult result = testUnit.execute(noParameters);
        CommandTestUtilities.checkSuccessfulCompletion(result);
        verify(warnService, times(1)).decayWarningsForServer(noParameters.getUserInitiatedContext().getServer());
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
