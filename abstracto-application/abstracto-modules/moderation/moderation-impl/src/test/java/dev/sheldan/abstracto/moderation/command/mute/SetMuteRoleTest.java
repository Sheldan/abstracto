package dev.sheldan.abstracto.moderation.command.mute;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.moderation.command.SetMuteRole;
import dev.sheldan.abstracto.moderation.service.management.MuteRoleManagementService;
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

    @Mock
    private ServerManagementService serverManagementService;

    @Test
    public void testExecuteCommand() {
        Role role = Mockito.mock(Role.class);
        Long roleId = 5L;
        when(role.getIdLong()).thenReturn(roleId);
        ARole aRole = Mockito.mock(ARole.class);
        when(roleManagementService.findRole(roleId)).thenReturn(aRole);
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(role));
        AServer server = Mockito.mock(AServer.class);
        when(serverManagementService.loadServer(parameters.getGuild())).thenReturn(server);
        CommandResult result = testUnit.execute(parameters);
        verify(muteRoleManagementService, times(1)).setMuteRoleForServer(server, aRole);
        CommandTestUtilities.checkSuccessfulCompletion(result);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
