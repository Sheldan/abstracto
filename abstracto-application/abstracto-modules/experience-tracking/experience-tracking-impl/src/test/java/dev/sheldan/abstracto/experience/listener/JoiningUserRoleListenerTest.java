package dev.sheldan.abstracto.experience.listener;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.experience.ExperienceRelatedTest;
import dev.sheldan.abstracto.experience.models.database.AUserExperience;
import dev.sheldan.abstracto.experience.service.AUserExperienceService;
import dev.sheldan.abstracto.experience.service.management.UserExperienceManagementService;
import dev.sheldan.abstracto.test.MockUtils;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JoiningUserRoleListenerTest extends ExperienceRelatedTest {

    @InjectMocks
    private JoiningUserRoleListener testUnit;

    @Mock
    private UserExperienceManagementService userExperienceManagementService;

    @Mock
    private AUserExperienceService userExperienceService;

    @Mock
    private JDAImpl jda;

    @Test
    public void testUserWithExperienceRejoining() {
        AServer server = AServer.builder().id(3L).build();
        AUser user = AUser.builder().id(1L).build();
        AUserInAServer aUserInAServer = AUserInAServer.builder().userInServerId(2L).userReference(user).build();
        MemberImpl member = MockUtils.getMockedMember(server, aUserInAServer, jda);
        GuildImpl guild = MockUtils.getGuild(server, jda);
        AUserExperience experience = AUserExperience.builder().experience(3L).user(aUserInAServer).build();
        when(userExperienceManagementService.findUserInServer(aUserInAServer)).thenReturn(experience);
        testUnit.execute(member, guild, aUserInAServer);
        verify(userExperienceService, times(1)).syncForSingleUser(experience);
    }

    @Test
    public void testUserWithOutExperienceRejoining() {
        AServer server = AServer.builder().id(3L).build();
        AUser user = AUser.builder().id(1L).build();
        AUserInAServer aUserInAServer = AUserInAServer.builder().userInServerId(2L).userReference(user).build();
        MemberImpl member = MockUtils.getMockedMember(server, aUserInAServer, jda);
        GuildImpl guild = MockUtils.getGuild(server, jda);
        AUserExperience experience = AUserExperience.builder().experience(3L).user(aUserInAServer).build();
        when(userExperienceManagementService.findUserInServer(aUserInAServer)).thenReturn(null);
        testUnit.execute(member, guild, aUserInAServer);
        verify(userExperienceService, times(0)).syncForSingleUser(experience);
    }

}
