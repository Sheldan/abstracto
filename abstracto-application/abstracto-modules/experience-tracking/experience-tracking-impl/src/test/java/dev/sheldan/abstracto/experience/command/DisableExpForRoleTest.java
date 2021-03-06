package dev.sheldan.abstracto.experience.command;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.experience.service.management.DisabledExpRoleManagementService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class DisableExpForRoleTest {

    @InjectMocks
    private DisableExpForRole testUnit;

    @Mock
    private RoleManagementService roleManagementService;

    @Mock
    private DisabledExpRoleManagementService disabledExpRoleManagementService;

    @Test
    public void testExecuteCommandForNotDisabledRole() {
        executeDisableExpForRoleTest(false, 1);
    }

    @Test
    public void testExecuteCommandForDisabledRole() {
        executeDisableExpForRoleTest(true, 0);
    }

    private void executeDisableExpForRoleTest(boolean value, int wantedNumberOfInvocations) {
        ARole parameterRole = Mockito.mock(ARole.class);
        ARole actualRole = Mockito.mock(ARole.class);
        when(parameterRole.getId()).thenReturn(5L);
        CommandContext context = CommandTestUtilities.getWithParameters(Arrays.asList(parameterRole));
        AServer server = Mockito.mock(AServer.class);
        when(actualRole.getServer()).thenReturn(server);
        when(roleManagementService.findRole(parameterRole.getId())).thenReturn(actualRole);
        when(disabledExpRoleManagementService.isExperienceDisabledForRole(actualRole)).thenReturn(value);
        CommandResult result = testUnit.execute(context);
        verify(disabledExpRoleManagementService, times(wantedNumberOfInvocations)).setRoleToBeDisabledForExp(actualRole);
        CommandTestUtilities.checkSuccessfulCompletion(result);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
