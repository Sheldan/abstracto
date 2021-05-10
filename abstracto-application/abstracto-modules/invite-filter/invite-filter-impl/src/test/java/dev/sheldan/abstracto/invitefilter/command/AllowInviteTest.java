package dev.sheldan.abstracto.invitefilter.command;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.invitefilter.service.InviteLinkFilterServiceBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AllowInviteTest {

    @InjectMocks
    private AllowInvite testUnit;

    @Mock
    private InviteLinkFilterServiceBean inviteLinkFilterServiceBean;

    private static final String INVITE_STRING = "invite";

    @Test
    public void testExecuteCommand() {
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(INVITE_STRING));
        when(inviteLinkFilterServiceBean.allowInvite(INVITE_STRING, parameters.getGuild().getIdLong(), parameters.getJda()))
                .thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(parameters);
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }

}
