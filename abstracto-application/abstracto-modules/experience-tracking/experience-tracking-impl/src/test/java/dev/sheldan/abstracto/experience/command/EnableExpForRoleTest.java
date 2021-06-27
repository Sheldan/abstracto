package dev.sheldan.abstracto.experience.command;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.experience.service.management.DisabledExpRoleManagementService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EnableExpForRoleTest {

    @InjectMocks
    private EnableExpForRole testUnit;

    @Mock
    private DisabledExpRoleManagementService disabledExpRoleManagementService;

    @Mock
    private RoleManagementService roleManagementService;

    @Test
    public void testExecuteCommandForNotDisabledRole() {
        executeEnableExpForRoleTest(false, 0);
    }

    @Test
    public void testExecuteCommandForDisabledRole() {
        executeEnableExpForRoleTest(true, 1);
    }

    private void executeEnableExpForRoleTest(boolean value, int wantedNumberOfInvocations) {
        ARole roleParameter = Mockito.mock(ARole.class);
        CommandContext context = CommandTestUtilities.getWithParameters(Arrays.asList(roleParameter));
        Long roleId = 8L;
        AServer server = Mockito.mock(AServer.class);
        ARole actualRole = Mockito.mock(ARole.class);
        when(actualRole.getServer()).thenReturn(server);
        when(roleParameter.getId()).thenReturn(roleId);
        when(roleManagementService.findRole(roleId)).thenReturn(actualRole);
        when(disabledExpRoleManagementService.isExperienceDisabledForRole(actualRole)).thenReturn(value);
        CommandResult result = testUnit.execute(context);
        verify(disabledExpRoleManagementService, times(wantedNumberOfInvocations)).removeRoleToBeDisabledForExp(actualRole);
        CommandTestUtilities.checkSuccessfulCompletion(result);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
