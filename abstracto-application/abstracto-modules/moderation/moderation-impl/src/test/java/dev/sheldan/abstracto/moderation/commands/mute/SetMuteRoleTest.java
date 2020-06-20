package dev.sheldan.abstracto.moderation.commands.mute;

import dev.sheldan.abstracto.core.command.exception.IncorrectParameter;
import dev.sheldan.abstracto.core.command.exception.InsufficientParameters;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.moderation.service.management.MuteRoleManagementService;
import dev.sheldan.abstracto.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.test.command.CommandTestUtilities;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SetMuteRoleTest {

    @InjectMocks
    private SetMuteRole testUnit;

    @Mock
    private MuteRoleManagementService muteRoleManagementService;

    @Test
    public void testExecuteCommand() {
        ARole muteRoleToSet = ARole.builder().build();
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(muteRoleToSet));
        CommandResult result = testUnit.execute(parameters);
        verify(muteRoleManagementService, times(1)).setMuteRoleForServer(parameters.getUserInitiatedContext().getServer(), muteRoleToSet);
        CommandTestUtilities.checkSuccessfulCompletion(result);
    }

    @Test(expected = InsufficientParameters.class)
    public void testTooLittleParameters() {
        CommandTestUtilities.executeNoParametersTest(testUnit);
    }

    @Test(expected = IncorrectParameter.class)
    public void testIncorrectParameterType() {
        CommandTestUtilities.executeWrongParametersTest(testUnit);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
