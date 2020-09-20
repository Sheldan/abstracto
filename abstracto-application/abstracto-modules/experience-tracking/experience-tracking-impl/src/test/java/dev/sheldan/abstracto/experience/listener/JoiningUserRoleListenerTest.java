package dev.sheldan.abstracto.experience.listener;

import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.experience.ExperienceRelatedTest;
import dev.sheldan.abstracto.experience.models.database.AUserExperience;
import dev.sheldan.abstracto.experience.service.AUserExperienceService;
import dev.sheldan.abstracto.experience.service.management.UserExperienceManagementService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JoiningUserRoleListenerTest extends ExperienceRelatedTest {

    @InjectMocks
    private JoiningUserRoleListener testUnit;

    @Mock
    private UserExperienceManagementService userExperienceManagementService;

    @Mock
    private AUserExperienceService userExperienceService;

    @Test
    public void testUserWithExperienceRejoining() {
        AUser user = AUser.builder().id(1L).build();
        AUserInAServer aUserInAServer = AUserInAServer.builder().userInServerId(2L).userReference(user).build();
        Member member = Mockito.mock(Member.class);
        User jdaUser = Mockito.mock(User.class);
        when(member.getUser()).thenReturn(jdaUser);
        when(jdaUser.getIdLong()).thenReturn(user.getId());
        Guild guild = Mockito.mock(Guild.class);
        AUserExperience experience = AUserExperience.builder().experience(3L).user(aUserInAServer).build();
        when(userExperienceManagementService.findUserInServer(aUserInAServer)).thenReturn(experience);
        when(userExperienceService.syncForSingleUser(experience)).thenReturn(CompletableFuture.completedFuture(null));
        testUnit.execute(member, guild, aUserInAServer);
    }

    @Test
    public void testUserWithOutExperienceRejoining() {
        AUser user = AUser.builder().id(1L).build();
        AUserInAServer aUserInAServer = AUserInAServer.builder().userInServerId(2L).userReference(user).build();
        Member member = Mockito.mock(Member.class);
        Guild guild = Mockito.mock(Guild.class);
        AUserExperience experience = AUserExperience.builder().experience(3L).user(aUserInAServer).build();
        when(userExperienceManagementService.findUserInServer(aUserInAServer)).thenReturn(null);
        testUnit.execute(member, guild, aUserInAServer);
        verify(userExperienceService, times(0)).syncForSingleUser(experience);
    }

}
