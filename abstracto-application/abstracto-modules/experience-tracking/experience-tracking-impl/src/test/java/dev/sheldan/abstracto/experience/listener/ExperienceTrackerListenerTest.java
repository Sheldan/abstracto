package dev.sheldan.abstracto.experience.listener;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.experience.ExperienceRelatedTest;
import dev.sheldan.abstracto.experience.service.AUserExperienceService;
import dev.sheldan.abstracto.test.MockUtils;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;
import net.dv8tion.jda.internal.entities.ReceivedMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ExperienceTrackerListenerTest extends ExperienceRelatedTest {

    @InjectMocks
    public ExperienceTrackerListener testUnit;

    @Mock
    private AUserExperienceService userExperienceService;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    @Mock
    private JDAImpl jda;

    @Test
    public void testExperienceTracking() {
        AServer server = AServer.builder().id(3L).build();
        AUser user = AUser.builder().id(4L).build();
        AUserInAServer userInAServer = AUserInAServer.builder().userReference(user).serverReference(server).build();
        MemberImpl member = MockUtils.getMockedMember(server, userInAServer, jda);
        ReceivedMessage mockedMessage = MockUtils.buildMockedMessage(1L, "text", member);
        when(userInServerManagementService.loadUser(member)).thenReturn(userInAServer);
        testUnit.execute(mockedMessage);
        verify(userExperienceService, times(1)).addExperience(userInAServer);
    }
}
