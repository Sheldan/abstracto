package dev.sheldan.abstracto.moderation.commands.invite;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.moderation.service.InviteLinkFilterServiceBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DisAllowInviteTest {

    @InjectMocks
    private DisAllowInvite testUnit;

    @Mock
    private InviteLinkFilterServiceBean inviteLinkFilterServiceBean;

    private static final String INVITE_STRING = "invite";

    @Test
    public void testExecuteCommand() {
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(INVITE_STRING));
        CommandResult result = testUnit.execute(parameters);
        CommandTestUtilities.checkSuccessfulCompletion(result);
        verify(inviteLinkFilterServiceBean, times(1)).disAllowInvite(INVITE_STRING, parameters.getGuild().getIdLong());
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }

}
