package dev.sheldan.abstracto.experience.commands;

import dev.sheldan.abstracto.core.command.exception.IncorrectParameterTypeException;
import dev.sheldan.abstracto.core.command.exception.InsufficientParametersException;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import dev.sheldan.abstracto.experience.service.management.DisabledExpRoleManagementService;
import dev.sheldan.abstracto.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.test.command.CommandTestUtilities;
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

    @Test(expected = InsufficientParametersException.class)
    public void testTooLittleParameters() {
        CommandTestUtilities.executeNoParametersTest(testUnit);
    }

    @Test(expected = IncorrectParameterTypeException.class)
    public void testIncorrectParameterType() {
        CommandTestUtilities.executeWrongParametersTest(testUnit);
    }

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
        when(roleParameter.getId()).thenReturn(roleId);
        ARole actualRole = Mockito.mock(ARole.class);
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
