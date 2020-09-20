package dev.sheldan.abstracto.experience.commands;

import dev.sheldan.abstracto.core.command.exception.IncorrectParameterException;
import dev.sheldan.abstracto.core.command.exception.InsufficientParametersException;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.exception.RoleNotFoundInGuildException;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.experience.service.ExperienceRoleService;
import dev.sheldan.abstracto.test.MockUtils;
import dev.sheldan.abstracto.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.test.command.CommandTestUtilities;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

    @Test(expected = InsufficientParametersException.class)
    public void testTooLittleParameters() {
        CommandTestUtilities.executeNoParametersTestAsync(testUnit);
    }

    @Test(expected = InsufficientParametersException.class)
    public void testRoleMissing() {
        CommandContext context = CommandTestUtilities.getWithParameters(Arrays.asList(4));
        testUnit.executeAsync(context);
    }

    @Test(expected = IncorrectParameterException.class)
    public void testIncorrectParameterType() {
        CommandTestUtilities.executeWrongParametersTestAsync(testUnit);
    }

    @Test(expected = IncorrectParameterException.class)
    public void testLevelProvidedButNotRole() {
        CommandContext context = CommandTestUtilities.getWithParameters(Arrays.asList(4, ""));
        testUnit.executeAsync(context);
    }

    @Test
    public void setExpRole() {
        CommandContext noParameters = CommandTestUtilities.getNoParameters();
        ARole changedRole = MockUtils.getRole(4L, noParameters.getUserInitiatedContext().getServer());
        Integer levelToSetTo = 4;
        CommandContext context = CommandTestUtilities.enhanceWithParameters(noParameters, Arrays.asList(levelToSetTo, changedRole));
        when(roleService.isRoleInServer(changedRole)).thenReturn(true);
        when(experienceRoleService.setRoleToLevel(changedRole, levelToSetTo, context.getUserInitiatedContext().getChannel())).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(context);
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test(expected = RoleNotFoundInGuildException.class)
    public void setExpRoleNotExistingOnServer() {
        CommandContext noParameters = CommandTestUtilities.getNoParameters();
        ARole changedRole = MockUtils.getRole(4L, noParameters.getUserInitiatedContext().getServer());
        Integer levelToSetTo = 4;
        CommandContext context = CommandTestUtilities.enhanceWithParameters(noParameters, Arrays.asList(levelToSetTo, changedRole));
        when(roleService.isRoleInServer(changedRole)).thenReturn(false);
        testUnit.executeAsync(context);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
