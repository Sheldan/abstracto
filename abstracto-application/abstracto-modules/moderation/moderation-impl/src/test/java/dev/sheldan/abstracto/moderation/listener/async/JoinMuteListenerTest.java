package dev.sheldan.abstracto.moderation.listener.async;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.moderation.service.MuteService;
import dev.sheldan.abstracto.moderation.service.management.MuteManagementService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JoinMuteListenerTest {

    @InjectMocks
    private JoinMuteListener testUnit;

    @Mock
    private MuteManagementService muteManagementService;

    @Mock
    private MuteService muteService;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    @Mock
    private Member member;

    @Mock
    private Guild guild;

    @Mock
    private AUserInAServer joiningUser;

    @Mock
    private ServerUser serverUser;

    private static final Long SERVER_ID = 3L;
    private static final Long USER_ID = 4L;

    @Test
    public void testNonMutedUserJoins() {
        when(serverUser.getServerId()).thenReturn(SERVER_ID);
        when(serverUser.getUserId()).thenReturn(USER_ID);
        when(userInServerManagementService.loadUser(SERVER_ID, USER_ID)).thenReturn(joiningUser);
        when(muteManagementService.hasActiveMute(joiningUser)).thenReturn(false);
        testUnit.execute(serverUser);
        verify(muteService, times(0)).applyMuteRole(joiningUser);
    }

    @Test
    public void testMutedUserJoins() {
        when(serverUser.getServerId()).thenReturn(SERVER_ID);
        when(serverUser.getUserId()).thenReturn(USER_ID);
        when(userInServerManagementService.loadUser(SERVER_ID, USER_ID)).thenReturn(joiningUser);
        when(muteManagementService.hasActiveMute(joiningUser)).thenReturn(true);
        testUnit.execute(serverUser);
        verify(muteService, times(1)).applyMuteRole(joiningUser);
    }
}
