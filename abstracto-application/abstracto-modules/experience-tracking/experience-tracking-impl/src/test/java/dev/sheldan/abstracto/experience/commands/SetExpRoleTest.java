package dev.sheldan.abstracto.experience.commands;

import dev.sheldan.abstracto.core.command.exception.IncorrectParameterTypeException;
import dev.sheldan.abstracto.core.command.exception.InsufficientParametersException;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import dev.sheldan.abstracto.experience.service.ExperienceRoleService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import net.dv8tion.jda.api.entities.Role;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

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

    @Test(expected = InsufficientParametersException.class)
    public void testTooLittleParameters() {
        CommandTestUtilities.executeNoParametersTestAsync(testUnit);
    }

    @Test(expected = InsufficientParametersException.class)
    public void testRoleMissing() {
        CommandContext context = CommandTestUtilities.getWithParameters(Arrays.asList(4));
        testUnit.executeAsync(context);
    }

    @Test(expected = IncorrectParameterTypeException.class)
    public void testIncorrectParameterType() {
        CommandTestUtilities.executeWrongParametersTestAsync(testUnit);
    }

    @Test(expected = IncorrectParameterTypeException.class)
    public void testLevelProvidedButNotRole() {
        CommandContext context = CommandTestUtilities.getWithParameters(Arrays.asList(4, ""));
        testUnit.executeAsync(context);
    }

    @Test
    public void setExpRole() {
        CommandContext noParameters = CommandTestUtilities.getNoParameters();
        Role roleToChange = Mockito.mock(Role.class);
        when(roleToChange.getGuild()).thenReturn(noParameters.getGuild());
        Integer levelToSetTo = 4;
        CommandContext context = CommandTestUtilities.enhanceWithParameters(noParameters, Arrays.asList(levelToSetTo, roleToChange));
        when(experienceRoleService.setRoleToLevel(roleToChange, levelToSetTo, context.getUserInitiatedContext().getChannel())).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(context);
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
