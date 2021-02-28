package dev.sheldan.abstracto.moderation.commands;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.moderation.service.management.UserNoteManagementService;
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
public class UserNoteCommandTest {

    @InjectMocks
    private UserNoteCommand testUnit;

    @Mock
    private UserNoteManagementService userNoteManagementService;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    @Test
    public void testExecuteUserNoteCommand() {
        Member member = Mockito.mock(Member.class);
        AUserInAServer userInAServer = Mockito.mock(AUserInAServer.class);
        String note = "note";
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(member, note));
        when(userInServerManagementService.loadOrCreateUser(member)).thenReturn(userInAServer);
        CommandResult result = testUnit.execute(parameters);
        verify(userNoteManagementService, times(1)).createUserNote(userInAServer, note);
        CommandTestUtilities.checkSuccessfulCompletion(result);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }

}
