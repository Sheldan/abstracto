package dev.sheldan.abstracto.experience.commands;

import dev.sheldan.abstracto.core.command.exception.IncorrectParameterTypeException;
import dev.sheldan.abstracto.core.command.exception.InsufficientParametersException;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.experience.service.AUserExperienceService;
import dev.sheldan.abstracto.core.test.MockUtils;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import net.dv8tion.jda.api.entities.Member;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EnableExpGainTest {

    @InjectMocks
    private EnableExpGain testUnit;

    @Mock
    private AUserExperienceService aUserExperienceService;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    @Test(expected = InsufficientParametersException.class)
    public void testTooLittleParameters() {
        CommandTestUtilities.executeNoParametersTest(testUnit);
    }

    @Test(expected = IncorrectParameterTypeException.class)
    public void testIncorrectParameterType() {
        CommandTestUtilities.executeWrongParametersTest(testUnit);
    }

    @Test
    public void testEnableExpForMember() {
        CommandContext noParameters = CommandTestUtilities.getNoParameters();
        AUserInAServer parameterUser = MockUtils.getUserObject(4L, noParameters.getUserInitiatedContext().getServer());
        Member member = Mockito.mock(Member.class);
        CommandContext context = CommandTestUtilities.enhanceWithParameters(noParameters, Arrays.asList(member));
        when(userInServerManagementService.loadUser(member)).thenReturn(parameterUser);
        CommandResult result = testUnit.execute(context);
        verify(aUserExperienceService, times(1)).enableExperienceForUser(parameterUser);
        CommandTestUtilities.checkSuccessfulCompletion(result);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }

}
