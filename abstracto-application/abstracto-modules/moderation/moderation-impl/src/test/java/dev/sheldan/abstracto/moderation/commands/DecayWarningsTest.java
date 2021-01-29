package dev.sheldan.abstracto.moderation.commands;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.moderation.service.WarnService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class DecayWarningsTest {

    @InjectMocks
    private DecayWarnings testUnit;

    @Mock
    private WarnService warnService;

    @Mock
    private ServerManagementService serverManagementService;

    @Test
    public void testExecuteCommand() {
        CommandContext noParameters = CommandTestUtilities.getNoParameters();
        AServer server = Mockito.mock(AServer.class);
        when(serverManagementService.loadServer(noParameters.getGuild())).thenReturn(server);
        when(warnService.decayWarningsForServer(server)).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(noParameters);
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
