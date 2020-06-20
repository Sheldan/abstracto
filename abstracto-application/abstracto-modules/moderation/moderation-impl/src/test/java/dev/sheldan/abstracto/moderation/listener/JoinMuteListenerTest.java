package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
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
    private Member member;

    @Mock
    private Guild guild;

    @Mock
    private AUserInAServer joiningUser;

    @Test
    public void testNonMutedUserJoins() {
        when(muteManagementService.hasActiveMute(joiningUser)).thenReturn(false);
        testUnit.execute(member, guild, joiningUser);
        verify(muteService, times(0)).applyMuteRole(joiningUser);
    }

    @Test
    public void testMutedUserJoins() {
        when(muteManagementService.hasActiveMute(joiningUser)).thenReturn(true);
        testUnit.execute(member, guild, joiningUser);
        verify(muteService, times(1)).applyMuteRole(joiningUser);
    }
}
