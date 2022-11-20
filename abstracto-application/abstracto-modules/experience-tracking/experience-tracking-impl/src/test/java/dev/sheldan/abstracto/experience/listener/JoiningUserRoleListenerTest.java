package dev.sheldan.abstracto.experience.listener;

import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.listener.MemberJoinModel;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.experience.model.database.AUserExperience;
import dev.sheldan.abstracto.experience.service.AUserExperienceService;
import dev.sheldan.abstracto.experience.service.management.UserExperienceManagementService;
import net.dv8tion.jda.api.entities.Member;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JoiningUserRoleListenerTest {

    @InjectMocks
    private JoiningUserRoleListener testUnit;

    @Mock
    private UserExperienceManagementService userExperienceManagementService;

    @Mock
    private AUserExperienceService userExperienceService;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    @Mock
    private ServerUser serverUser;

    @Mock
    private AUserInAServer aUserInAServer;

    @Mock
    private MemberJoinModel model;

    @Mock
    private Member member;

    private static final Long SERVER_ID = 1L;
    private static final Long USER_ID = 2L;
    private static final Long USER_IN_SERVER_ID = 3L;

    @Before
    public void setup() {
        when(serverUser.getUserId()).thenReturn(USER_ID);
        when(model.getJoiningUser()).thenReturn(serverUser);
        when(model.getServerId()).thenReturn(SERVER_ID);
        when(aUserInAServer.getUserInServerId()).thenReturn(USER_IN_SERVER_ID);
        when(userInServerManagementService.loadUserOptional(SERVER_ID, USER_ID)).thenReturn(Optional.of(aUserInAServer));
    }

    @Test
    public void testUserWithExperienceRejoining() {
        AUserExperience experience = Mockito.mock(AUserExperience.class);
        when(userExperienceManagementService.findByUserInServerIdOptional(USER_IN_SERVER_ID)).thenReturn(Optional.of(experience));
        when(userExperienceService.syncForSingleUser(experience, member)).thenReturn(CompletableFuture.completedFuture(null));
        when(model.getMember()).thenReturn(member);
        DefaultListenerResult result = testUnit.execute(model);
        Assert.assertEquals(DefaultListenerResult.PROCESSED, result);
    }

    @Test
    public void testUserWithOutExperienceRejoining() {
        when(userExperienceManagementService.findByUserInServerIdOptional(USER_IN_SERVER_ID)).thenReturn(Optional.empty());
        testUnit.execute(model);
        verify(userExperienceService, times(0)).syncForSingleUser(any(), any());
    }

}
