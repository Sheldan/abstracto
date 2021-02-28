package dev.sheldan.abstracto.moderation.commands;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.moderation.models.database.Warning;
import dev.sheldan.abstracto.moderation.service.management.WarnManagementService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
    private static final Long SERVER_ID = 1L;

    @Test
    public void testDeleteExistingWarning() {
        Warning existingWarning = Mockito.mock(Warning.class);
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(WARN_ID));
        when(parameters.getGuild().getIdLong()).thenReturn(SERVER_ID);
        when(warnManagementService.findByIdOptional(WARN_ID, SERVER_ID)).thenReturn(Optional.of(existingWarning));
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

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
