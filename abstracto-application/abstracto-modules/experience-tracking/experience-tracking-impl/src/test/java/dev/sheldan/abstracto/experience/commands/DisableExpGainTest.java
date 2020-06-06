package dev.sheldan.abstracto.experience.commands;

import dev.sheldan.abstracto.core.command.exception.IncorrectParameter;
import dev.sheldan.abstracto.core.command.exception.InsufficientParameters;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.experience.service.AUserExperienceService;
import dev.sheldan.abstracto.test.MockUtils;
import dev.sheldan.abstracto.test.command.CommandTestUtilities;
import net.dv8tion.jda.api.entities.Member;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

import java.util.Arrays;

@RunWith(MockitoJUnitRunner.class)
public class DisableExpGainTest {

    @InjectMocks
    private DisableExpGain testUnit;

    @Mock
    private AUserExperienceService aUserExperienceService;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    @Test(expected = InsufficientParameters.class)
    public void testTooLittleParameters() {
        CommandTestUtilities.executeNoParametersTest(testUnit);
    }

    @Test(expected = IncorrectParameter.class)
    public void testIncorrectParameterType() {
        CommandTestUtilities.executeWrongParametersTest(testUnit);
    }

    @Test
    public void testDisableExpForMember() {
        CommandContext noParameters = CommandTestUtilities.getNoParameters();
        AUserInAServer parameterUser = MockUtils.getUserObject(4L, noParameters.getUserInitiatedContext().getServer());
        Member member = Mockito.mock(Member.class);
        CommandContext context = CommandTestUtilities.enhanceWithParameters(noParameters, Arrays.asList(member));
        when(userInServerManagementService.loadUser(member)).thenReturn(parameterUser);
        CommandResult result = testUnit.execute(context);
        verify(aUserExperienceService, times(1)).disableExperienceForUser(parameterUser);
        CommandTestUtilities.checkSuccessfulCompletion(result);
    }

}
