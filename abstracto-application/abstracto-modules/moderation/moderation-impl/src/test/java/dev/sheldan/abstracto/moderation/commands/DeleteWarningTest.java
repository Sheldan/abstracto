package dev.sheldan.abstracto.moderation.commands;

import dev.sheldan.abstracto.core.command.exception.IncorrectParameterTypeException;
import dev.sheldan.abstracto.core.command.exception.InsufficientParametersException;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.moderation.models.database.Warning;
import dev.sheldan.abstracto.moderation.service.management.WarnManagementService;
import dev.sheldan.abstracto.core.test.MockUtils;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DeleteWarningTest {
    @InjectMocks
    private DeleteWarning testUnit;

    @Mock
    private WarnManagementService warnManagementService;

    private static final Long WARN_ID = 5L;

    @Test
    public void testDeleteExistingWarning() {
        AServer server = MockUtils.getServer();
        AUserInAServer warnedUser = MockUtils.getUserObject(5L, server);
        AUserInAServer warningUser = MockUtils.getUserObject(6L, server);
        Warning existingWarning = Warning.builder().warnedUser(warnedUser).warningUser(warningUser).build();
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(WARN_ID));
        when(warnManagementService.findByIdOptional(WARN_ID, parameters.getGuild().getIdLong())).thenReturn(Optional.of(existingWarning));
        CommandResult result = testUnit.execute(parameters);
        verify(warnManagementService, times(1)).deleteWarning(existingWarning);
        CommandTestUtilities.checkSuccessfulCompletion(result);
    }

    @Test
    public void testDeleteNotExistingWarning() {
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(WARN_ID));
        CommandResult result = testUnit.execute(parameters);
        CommandTestUtilities.checkSuccessfulCompletion(result);
    }


    @Test(expected = InsufficientParametersException.class)
    public void testTooLittleParameters() {
        CommandTestUtilities.executeNoParametersTest(testUnit);
    }

    @Test(expected = IncorrectParameterTypeException.class)
    public void testIncorrectParameterType() {
        CommandTestUtilities.executeWrongParametersTest(testUnit);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
