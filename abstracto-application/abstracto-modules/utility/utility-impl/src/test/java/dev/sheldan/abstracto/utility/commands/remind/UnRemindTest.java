package dev.sheldan.abstracto.utility.commands.remind;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.utility.service.ReminderService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UnRemindTest {

    @InjectMocks
    private UnRemind testUnit;

    @Mock
    private ReminderService reminderService;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    @Test
    public void testExecuteCommand() {
        Long reminderId = 6L;
        CommandContext withParameters = CommandTestUtilities.getWithParameters(Arrays.asList(reminderId));
        AUserInAServer user = Mockito.mock(AUserInAServer.class);
        when(userInServerManagementService.loadOrCreateUser(withParameters.getAuthor())).thenReturn(user);
        CommandResult result = testUnit.execute(withParameters);
        verify(reminderService, times(1)).unRemind(reminderId, user);
        CommandTestUtilities.checkSuccessfulCompletion(result);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
