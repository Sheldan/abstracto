package dev.sheldan.abstracto.experience.command;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.experience.service.AUserExperienceService;
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
public class DisableExpGainTest {

    @InjectMocks
    private DisableExpGain testUnit;

    @Mock
    private AUserExperienceService aUserExperienceService;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    @Test
    public void testDisableExpForMember() {
        CommandContext noParameters = CommandTestUtilities.getNoParameters();
        AUserInAServer parameterUser = Mockito.mock(AUserInAServer.class);
        Member member = Mockito.mock(Member.class);
        when(member.getGuild()).thenReturn(noParameters.getGuild());
        CommandContext context = CommandTestUtilities.enhanceWithParameters(noParameters, Arrays.asList(member));
        when(userInServerManagementService.loadOrCreateUser(member)).thenReturn(parameterUser);
        CommandResult result = testUnit.execute(context);
        verify(aUserExperienceService, times(1)).disableExperienceForUser(parameterUser);
        CommandTestUtilities.checkSuccessfulCompletion(result);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
