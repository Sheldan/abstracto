package dev.sheldan.abstracto.experience.command;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.experience.service.AUserExperienceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SyncRolesTest {

    @InjectMocks
    private SyncRoles testUnit;

    @Mock
    private AUserExperienceService userExperienceService;

    @Mock
    private ChannelManagementService channelManagementService;

    @Mock
    private ServerManagementService serverManagementService;

    private static final Long CHANNEL_ID = 4L;

    @Test
    public void executeCommand() {
        CommandContext context = CommandTestUtilities.getNoParameters();
        AServer server = Mockito.mock(AServer.class);
        when(serverManagementService.loadServer(context.getGuild())).thenReturn(server);
        when(context.getChannel().getIdLong()).thenReturn(CHANNEL_ID);
        when(userExperienceService.syncUserRolesWithFeedback(server, CHANNEL_ID)).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(context);
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
