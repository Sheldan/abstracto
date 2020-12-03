package dev.sheldan.abstracto.moderation.commands.mute;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.moderation.exception.NoMuteFoundException;
import dev.sheldan.abstracto.moderation.service.MuteService;
import dev.sheldan.abstracto.moderation.service.management.MuteManagementService;
import dev.sheldan.abstracto.templating.service.TemplateService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import net.dv8tion.jda.api.entities.Member;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;


import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UnMuteTest {

    @InjectMocks
    private UnMute testUnit;

    @Mock
    private MuteService muteService;

    @Mock
    private MuteManagementService muteManagementService;

    @Mock
    private TemplateService templateService;

    @Mock
    private Member memberToUnMute;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    @Test
    public void testUnMuteCommand() {
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(memberToUnMute));
        AUserInAServer user = AUserInAServer.builder().build();
        when(userInServerManagementService.loadUser(memberToUnMute)).thenReturn(user);
        when(muteService.unMuteUser(user)).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<CommandResult> result = testUnit.executeAsync(parameters);
        CommandTestUtilities.checkSuccessfulCompletionAsync(result);
    }

    @Test(expected = NoMuteFoundException.class)
    public void testUnMuteCommandWithoutExistingMute() {
        CommandContext parameters = CommandTestUtilities.getWithParameters(Arrays.asList(memberToUnMute));
        AUserInAServer user = AUserInAServer.builder().build();
        when(userInServerManagementService.loadUser(memberToUnMute)).thenReturn(user);
        when(muteService.unMuteUser(user)).thenThrow(new NoMuteFoundException());
        testUnit.executeAsync(parameters);
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
