package dev.sheldan.abstracto.experience.commands;

import dev.sheldan.abstracto.core.command.exception.IncorrectParameter;
import dev.sheldan.abstracto.core.command.exception.InsufficientParameters;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.exception.RoleNotFoundInGuildException;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.experience.service.ExperienceRoleService;
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
public class SetExpRoleTest {

    @InjectMocks
    private SetExpRole testUnit;

    @Mock
    private ExperienceRoleService experienceRoleService;

    @Mock
    private RoleService roleService;

    @Mock
    private JDAImpl jda;

    @Test(expected = InsufficientParameters.class)
    public void testTooLittleParameters() {
        CommandTestUtilities.executeNoParametersTest(testUnit, jda);
    }

    @Test(expected = InsufficientParameters.class)
    public void testRoleMissing() {
        CommandContext context = CommandTestUtilities.getWithParameters(jda, Arrays.asList(4));
        testUnit.execute(context);
    }

    @Test(expected = IncorrectParameter.class)
    public void testIncorrectParameterType() {
        CommandTestUtilities.executeWrongParametersTest(testUnit, jda);
    }

    @Test(expected = IncorrectParameter.class)
    public void testLevelProvidedButNotRole() {
        CommandContext context = CommandTestUtilities.getWithParameters(jda, Arrays.asList(4, ""));
        testUnit.execute(context);
    }

    @Test
    public void setExpRole() {
        CommandContext noParameters = CommandTestUtilities.getNoParameters(jda);
        ARole changedRole = MockUtils.getRole(4L, noParameters.getUserInitiatedContext().getServer());
        Integer levelToSetTo = 4;
        CommandContext context = CommandTestUtilities.enhanceWithParameters(noParameters, Arrays.asList(levelToSetTo, changedRole));
        when(roleService.isRoleInServer(changedRole)).thenReturn(true);
        CommandResult result = testUnit.execute(context);
        CommandTestUtilities.checkSuccessfulCompletion(result);
        verify(experienceRoleService, times(1)).setRoleToLevel(changedRole, levelToSetTo, context.getUserInitiatedContext().getChannel());
    }

    @Test(expected = RoleNotFoundInGuildException.class)
    public void setExpRoleNotExistingOnServer() {
        CommandContext noParameters = CommandTestUtilities.getNoParameters(jda);
        ARole changedRole = MockUtils.getRole(4L, noParameters.getUserInitiatedContext().getServer());
        Integer levelToSetTo = 4;
        CommandContext context = CommandTestUtilities.enhanceWithParameters(noParameters, Arrays.asList(levelToSetTo, changedRole));
        when(roleService.isRoleInServer(changedRole)).thenReturn(false);
        testUnit.execute(context);
    }
}
