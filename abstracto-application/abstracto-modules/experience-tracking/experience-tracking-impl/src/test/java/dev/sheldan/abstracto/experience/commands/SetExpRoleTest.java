package dev.sheldan.abstracto.experience.commands;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.experience.service.ExperienceRoleService;
import net.dv8tion.jda.api.entities.Role;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SetExpRoleTest {

    @InjectMocks
    private SetExpRole testUnit;

    @Mock
    private ExperienceRoleService experienceRoleService;

    @Mock
    private RoleService roleService;

    @Mock
    private RoleManagementService roleManagementService;

    private static final Long CHANNEL_ID = 4L;

    @Test
    public void setExpRole() {
        CommandContext noParameters = CommandTestUtilities.getNoParameters();
        Role roleToChange = Mockito.mock(Role.class);
        when(roleToChange.getGuild()).thenReturn(noParameters.getGuild());
        Integer levelToSetTo = 4;
        when(noParameters.getChannel().getIdLong()).thenReturn(CHANNEL_ID);
        CommandContext context = CommandTestUtilities.enhanceWithParameters(noParameters, Arrays.asList(levelToSetTo, roleToChange));
        when(experienceRoleService.setRoleToLevel(roleToChange, levelToSetTo, CHANNEL_ID)).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(context);
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
