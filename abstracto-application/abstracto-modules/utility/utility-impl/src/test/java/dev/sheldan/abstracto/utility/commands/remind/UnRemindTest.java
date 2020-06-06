package dev.sheldan.abstracto.utility.commands.remind;

import dev.sheldan.abstracto.core.command.exception.IncorrectParameter;
import dev.sheldan.abstracto.core.command.exception.InsufficientParameters;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.utility.service.ReminderService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UnRemindTest {

    @InjectMocks
    private UnRemind testUnit;

    @Mock
    private ReminderService reminderService;

    @Test(expected = IncorrectParameter.class)
    public void testIncorrectParameterType() {
        CommandTestUtilities.executeWrongParametersTest(testUnit);
    }

    @Test(expected = InsufficientParameters.class)
    public void testTooLittleParameters() {
        CommandTestUtilities.executeNoParametersTest(testUnit);
    }

    @Test
    public void testExecuteCommand() {
        Long reminderId = 6L;
        CommandContext withParameters = CommandTestUtilities.getWithParameters(Arrays.asList(reminderId));
        CommandResult result = testUnit.execute(withParameters);
        verify(reminderService, times(1)).unRemind(reminderId, withParameters.getUserInitiatedContext().getAUserInAServer());
        CommandTestUtilities.checkSuccessfulCompletion(result);
    }
}
