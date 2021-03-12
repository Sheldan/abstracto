package dev.sheldan.abstracto.repostdetection.command;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.repostdetection.service.RepostService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PurgeRepostsTest {

    @InjectMocks
    private PurgeReposts testUnit;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    @Mock
    private RepostService repostService;

    @Test
    public void testPurgeImagePostsFromMember(){
        AUserInAServer fakeUser = Mockito.mock(AUserInAServer.class);
        AUserInAServer actualUser = Mockito.mock(AUserInAServer.class);
        Long userInServerId = 1L;
        when(fakeUser.getUserInServerId()).thenReturn(userInServerId);
        CommandContext purgeImagePostsParameters = CommandTestUtilities.getWithParameters(Arrays.asList(fakeUser));
        when(userInServerManagementService.loadOrCreateUser(userInServerId)).thenReturn(actualUser);
        CommandResult result = testUnit.execute(purgeImagePostsParameters);
        CommandTestUtilities.checkSuccessfulCompletion(result);
        verify(repostService, times(1)).purgeReposts(actualUser);
    }

    @Test
    public void testPurgeImagePostsFromServer(){
        CommandContext noParameters = CommandTestUtilities.getNoParameters();
        CommandResult result = testUnit.execute(noParameters);
        CommandTestUtilities.checkSuccessfulCompletion(result);
        verify(repostService, times(1)).purgeReposts(noParameters.getGuild());
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }
}
