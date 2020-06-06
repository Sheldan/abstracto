package dev.sheldan.abstracto.experience.commands;

import dev.sheldan.abstracto.core.command.exception.IncorrectParameter;
import dev.sheldan.abstracto.core.command.exception.InsufficientParameters;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.experience.service.management.DisabledExpRoleManagementService;
import dev.sheldan.abstracto.test.MockUtils;
import dev.sheldan.abstracto.test.command.CommandTestUtilities;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EnableExpForRoleTest {

    @InjectMocks
    private EnableExpForRole testUnit;

    @Mock
    private DisabledExpRoleManagementService disabledExpRoleManagementService;

    @Test(expected = InsufficientParameters.class)
    public void testTooLittleParameters() {
        CommandTestUtilities.executeNoParametersTest(testUnit);
    }

    @Test(expected = IncorrectParameter.class)
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
        ARole disabledRole = MockUtils.getRole(1L, MockUtils.getServer());
        CommandContext context = CommandTestUtilities.getWithParameters(Arrays.asList(disabledRole));
        when(disabledExpRoleManagementService.isExperienceDisabledForRole(disabledRole)).thenReturn(value);
        CommandResult result = testUnit.execute(context);
        verify(disabledExpRoleManagementService, times(wantedNumberOfInvocations)).removeRoleToBeDisabledForExp(disabledRole);
        CommandTestUtilities.checkSuccessfulCompletion(result);
    }
}
