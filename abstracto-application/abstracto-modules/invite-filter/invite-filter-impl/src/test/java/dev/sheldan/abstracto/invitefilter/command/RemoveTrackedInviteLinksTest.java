package dev.sheldan.abstracto.invitefilter.command;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.invitefilter.service.InviteLinkFilterService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RemoveTrackedInviteLinksTest {

    @InjectMocks
    private RemoveTrackedInviteLinks testUnit;

    @Mock
    private InviteLinkFilterService inviteLinkFilterService;

    private static final String INVITE_STRING = "invite";
    private static final Long SERVER_ID = 2L;

    @Test
    public void testExecuteCommandNoParameter() {
        CommandContext parameters = CommandTestUtilities.getNoParameters();
        CompletableFuture<CommandResult> result = testUnit.executeAsync(parameters);
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
        verify(inviteLinkFilterService, times(1)).clearAllTrackedInviteCodes(parameters.getGuild().getIdLong());
    }

    @Test
    public void testExecuteCommandForSpecificInvite() {
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(INVITE_STRING));
        when(parameters.getGuild().getIdLong()).thenReturn(SERVER_ID);
        when(inviteLinkFilterService.clearAllUsedOfCode(INVITE_STRING, SERVER_ID, parameters.getJda()))
                .thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(parameters);
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }

}
