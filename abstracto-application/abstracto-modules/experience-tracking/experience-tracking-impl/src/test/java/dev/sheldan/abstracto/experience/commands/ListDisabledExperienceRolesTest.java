package dev.sheldan.abstracto.experience.commands;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.experience.models.database.ADisabledExpRole;
import dev.sheldan.abstracto.experience.models.templates.DisabledExperienceRolesModel;
import dev.sheldan.abstracto.experience.service.management.DisabledExpRoleManagementService;
import net.dv8tion.jda.api.entities.Role;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ListDisabledExperienceRolesTest {

    @InjectMocks
    private ListDisabledExperienceRoles testUnit;

    @Mock
    private DisabledExpRoleManagementService disabledExpRoleManagementService;

    @Mock
    private RoleService roleService;

    @Mock
    private ChannelService channelService;

    @Mock
    private ServerManagementService serverManagementService;

    @Mock
    private AServer server;

    @Test
    public void testCommandExecutionNoRolesFound() {
        CommandContext context = CommandTestUtilities.getNoParameters();
        when(serverManagementService.loadServer(context.getGuild())).thenReturn(server);
        when(disabledExpRoleManagementService.getDisabledRolesForServer(server)).thenReturn(new ArrayList<>());
        when(channelService.sendEmbedTemplateInTextChannelList(eq("list_disabled_experience_roles"),
                any(DisabledExperienceRolesModel.class), eq(context.getChannel()))).thenReturn(CommandTestUtilities.messageFutureList());
        CompletableFuture<CommandResult> result = testUnit.executeAsync(context);
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
        verify(roleService, times(0)).getRoleFromGuild(any(ARole.class));
    }

    @Test
    public void testCommandExecutionRolesFound() {
        CommandContext context = CommandTestUtilities.getNoParameters();
        ADisabledExpRole disabledExpRole1 = Mockito.mock(ADisabledExpRole.class);
        ADisabledExpRole disabledExpRole2 = Mockito.mock(ADisabledExpRole.class);
        when(disabledExpRoleManagementService.getDisabledRolesForServer(server)).thenReturn(Arrays.asList(disabledExpRole1, disabledExpRole2));
        Role role1 = Mockito.mock(Role.class);
        Role role2 = Mockito.mock(Role.class);

        ARole aRole = Mockito.mock(ARole.class);
        ARole aRole2 = Mockito.mock(ARole.class);
        when(roleService.getRoleFromGuild(aRole)).thenReturn(role1);
        when(roleService.getRoleFromGuild(aRole2)).thenReturn(role2);
        when(aRole.getDeleted()).thenReturn(false);
        when(aRole2.getDeleted()).thenReturn(false);
        when(disabledExpRole1.getRole()).thenReturn(aRole);
        when(disabledExpRole2.getRole()).thenReturn(aRole2);
        when(serverManagementService.loadServer(context.getGuild())).thenReturn(server);
        when(channelService.sendEmbedTemplateInTextChannelList(eq("list_disabled_experience_roles"),
                any(DisabledExperienceRolesModel.class), eq(context.getChannel()))).thenReturn(CommandTestUtilities.messageFutureList());
        CompletableFuture<CommandResult> result = testUnit.executeAsync(context);
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
