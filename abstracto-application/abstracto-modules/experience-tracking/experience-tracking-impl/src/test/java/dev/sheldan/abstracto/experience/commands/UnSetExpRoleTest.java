package dev.sheldan.abstracto.experience.commands;

import dev.sheldan.abstracto.core.command.exception.IncorrectParameterTypeException;
import dev.sheldan.abstracto.core.command.exception.InsufficientParametersException;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import dev.sheldan.abstracto.experience.service.ExperienceRoleService;
import dev.sheldan.abstracto.test.MockUtils;
import dev.sheldan.abstracto.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.test.command.CommandTestUtilities;
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
public class UnSetExpRoleTest {

    @InjectMocks
    private UnSetExpRole testUnit;

    @Mock
    private ExperienceRoleService experienceRoleService;

    @Mock
    private RoleManagementService roleManagementService;

    @Test(expected = InsufficientParametersException.class)
    public void testTooLittleParameters() {
        CommandTestUtilities.executeNoParametersTestAsync(testUnit);
    }

    @Test(expected = IncorrectParameterTypeException.class)
    public void testIncorrectParameterType() {
        CommandTestUtilities.executeWrongParametersTestAsync(testUnit);
    }

    @Test
    public void setUnSetExpRole() {
        CommandContext noParameters = CommandTestUtilities.getNoParameters();
        ARole changedRole = MockUtils.getRole(4L, noParameters.getUserInitiatedContext().getServer());
        CommandContext context = CommandTestUtilities.enhanceWithParameters(noParameters, Arrays.asList(changedRole));
        ARole actualRole = Mockito.mock(ARole.class);
        when(roleManagementService.findRole(changedRole.getId())).thenReturn(actualRole);
        when(experienceRoleService.unsetRole(actualRole, context.getUserInitiatedContext().getChannel())).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(context);
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
