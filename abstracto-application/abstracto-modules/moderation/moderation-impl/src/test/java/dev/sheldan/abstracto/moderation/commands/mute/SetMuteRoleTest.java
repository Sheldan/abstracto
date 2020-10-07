package dev.sheldan.abstracto.moderation.commands.mute;

import dev.sheldan.abstracto.core.command.exception.IncorrectParameterTypeException;
import dev.sheldan.abstracto.core.command.exception.InsufficientParametersException;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import dev.sheldan.abstracto.moderation.service.management.MuteRoleManagementService;
import dev.sheldan.abstracto.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.test.command.CommandTestUtilities;
import net.dv8tion.jda.api.entities.Role;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SetMuteRoleTest {

    @InjectMocks
    private SetMuteRole testUnit;

    @Mock
    private MuteRoleManagementService muteRoleManagementService;

    @Mock
    private RoleManagementService roleManagementService;

    @Test
    public void testExecuteCommand() {
        Role role = Mockito.mock(Role.class);
        Long roleId = 5L;
        when(role.getIdLong()).thenReturn(roleId);
        ARole aRole = Mockito.mock(ARole.class);
        when(roleManagementService.findRole(roleId)).thenReturn(aRole);
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(role));
        CommandResult result = testUnit.execute(parameters);
        verify(muteRoleManagementService, times(1)).setMuteRoleForServer(parameters.getUserInitiatedContext().getServer(), aRole);
        CommandTestUtilities.checkSuccessfulCompletion(result);
    }

    @Test(expected = InsufficientParametersException.class)
    public void testTooLittleParameters() {
        CommandTestUtilities.executeNoParametersTest(testUnit);
    }

    @Test(expected = IncorrectParameterTypeException.class)
    public void testIncorrectParameterType() {
        CommandTestUtilities.executeWrongParametersTest(testUnit);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
