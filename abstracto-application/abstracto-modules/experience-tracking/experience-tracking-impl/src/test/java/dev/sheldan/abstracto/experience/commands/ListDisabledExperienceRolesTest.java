package dev.sheldan.abstracto.experience.commands;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.experience.models.database.ADisabledExpRole;
import dev.sheldan.abstracto.experience.models.templates.DisabledExperienceRolesModel;
import dev.sheldan.abstracto.experience.service.management.DisabledExpRoleManagementService;
import dev.sheldan.abstracto.test.MockUtils;
import dev.sheldan.abstracto.test.command.CommandTestUtilities;
import net.dv8tion.jda.internal.JDAImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;

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
    private JDAImpl jda;

    @Test
    public void testCommandExecutionNoRolesFound() {
        CommandContext context = CommandTestUtilities.getNoParameters(jda);
        AServer server = context.getUserInitiatedContext().getServer();
        when(disabledExpRoleManagementService.getDisabledRolesForServer(server)).thenReturn(new ArrayList<>());
        CommandResult result = testUnit.execute(context);
        CommandTestUtilities.checkSuccessfulCompletion(result);
        verify(roleService, times(0)).getRoleFromGuild(any(ARole.class));
        verify(channelService, times(1)).sendEmbedTemplateInChannel(eq("list_disabled_experience_roles"),
                any(DisabledExperienceRolesModel.class), eq(context.getChannel()));
    }

    @Test
    public void testCommandExecutionRolesFound() {
        CommandContext context = CommandTestUtilities.getNoParameters(jda);
        AServer server = context.getUserInitiatedContext().getServer();
        ADisabledExpRole disabledExpRole1 = ADisabledExpRole.builder().role(MockUtils.getRole(4L, server)).build();
        ADisabledExpRole disabledExpRole2 = ADisabledExpRole.builder().role(MockUtils.getRole(6L, server)).build();
        when(disabledExpRoleManagementService.getDisabledRolesForServer(server)).thenReturn(Arrays.asList(disabledExpRole1, disabledExpRole2));
        CommandResult result = testUnit.execute(context);
        CommandTestUtilities.checkSuccessfulCompletion(result);
        verify(roleService, times(2)).getRoleFromGuild(any(ARole.class));
        verify(channelService, times(1)).sendEmbedTemplateInChannel(eq("list_disabled_experience_roles"),
                any(DisabledExperienceRolesModel.class), eq(context.getChannel()));
    }
}
