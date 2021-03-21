package dev.sheldan.abstracto.experience.listener;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.listener.MemberJoinModel;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.experience.model.database.AUserExperience;
import dev.sheldan.abstracto.experience.service.AUserExperienceService;
import dev.sheldan.abstracto.experience.service.management.UserExperienceManagementService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

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

    private static final Long SERVER_ID = 1L;
    private static final Long USER_ID = 2L;

    @Before
    public void setup() {
        when(serverUser.getServerId()).thenReturn(SERVER_ID);
        when(serverUser.getUserId()).thenReturn(USER_ID);
        when(model.getJoiningUser()).thenReturn(serverUser);
        when(userInServerManagementService.loadOrCreateUser(SERVER_ID, USER_ID)).thenReturn(aUserInAServer);
    }

    @Test
    public void testUserWithExperienceRejoining() {
        AUserExperience experience = Mockito.mock(AUserExperience.class);
        when(userExperienceManagementService.findUserInServer(aUserInAServer)).thenReturn(experience);
        when(userExperienceService.syncForSingleUser(experience)).thenReturn(CompletableFuture.completedFuture(null));
        testUnit.execute(model);
    }

    @Test
    public void testUserWithOutExperienceRejoining() {
        when(userExperienceManagementService.findUserInServer(aUserInAServer)).thenReturn(null);
        testUnit.execute(model);
        verify(userExperienceService, times(0)).syncForSingleUser(any());
    }

}
