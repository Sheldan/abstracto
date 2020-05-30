package dev.sheldan.abstracto.experience.commands;

import dev.sheldan.abstracto.core.command.exception.IncorrectParameter;
import dev.sheldan.abstracto.core.command.exception.InsufficientParameters;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.experience.service.management.DisabledExpRoleManagementService;
import dev.sheldan.abstracto.test.MockUtils;
import dev.sheldan.abstracto.test.command.CommandTestUtilities;
import net.dv8tion.jda.internal.JDAImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DisableExpForRoleTest {

    @InjectMocks
    private DisableExpForRole testUnit;

    @Mock
    private DisabledExpRoleManagementService disabledExpRoleManagementService;

    @Mock
    private JDAImpl jda;

    @Test(expected = InsufficientParameters.class)
    public void testTooLittleParameters() {
        CommandTestUtilities.executeNoParametersTest(testUnit, jda);
    }

    @Test(expected = IncorrectParameter.class)
    public void testIncorrectParameterType() {
        CommandTestUtilities.executeWrongParametersTest(testUnit, jda);
    }

    @Test
    public void testExecuteCommandForNotDisabledRole() {
        executeDisableExpForRoleTest(false, 1);
    }

    @Test
    public void testExecuteCommandForDisabledRole() {
        executeDisableExpForRoleTest(true, 0);
    }

    private void executeDisableExpForRoleTest(boolean value, int wantedNumberOfInvocations) {
        ARole disabledRole = MockUtils.getRole(1L, MockUtils.getServer());
        CommandContext context = CommandTestUtilities.getWithParameters(jda, Arrays.asList(disabledRole));
        when(disabledExpRoleManagementService.isExperienceDisabledForRole(disabledRole)).thenReturn(value);
        CommandResult result = testUnit.execute(context);
        verify(disabledExpRoleManagementService, times(wantedNumberOfInvocations)).setRoleToBeDisabledForExp(disabledRole);
        CommandTestUtilities.checkSuccessfulCompletion(result);
    }
}
