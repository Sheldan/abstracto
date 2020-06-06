package dev.sheldan.abstracto.experience.commands;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.service.AUserExperienceService;
import dev.sheldan.abstracto.test.command.CommandTestUtilities;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SyncRolesTest {

    @InjectMocks
    private SyncRoles testUnit;

    @Mock
    private AUserExperienceService userExperienceService;

    @Test
    public void executeCommand() {
        CommandContext context = CommandTestUtilities.getNoParameters();
        CommandResult result = testUnit.execute(context);
        AServer server = context.getUserInitiatedContext().getServer();
        AChannel channel = context.getUserInitiatedContext().getChannel();
        verify(userExperienceService, times(1)).syncUserRolesWithFeedback(server, channel);
        CommandTestUtilities.checkSuccessfulCompletion(result);
    }
}
