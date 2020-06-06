package dev.sheldan.abstracto.experience.listener;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.experience.ExperienceRelatedTest;
import dev.sheldan.abstracto.experience.service.AUserExperienceService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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

    @Test
    public void testExperienceTracking() {
        AServer server = AServer.builder().id(3L).build();
        AUser user = AUser.builder().id(4L).build();
        AUserInAServer userInAServer = AUserInAServer.builder().userReference(user).serverReference(server).build();
        Member member = Mockito.mock(Member.class);
        Message mockedMessage = Mockito.mock(Message.class);
        when(mockedMessage.getMember()).thenReturn(member);
        when(userInServerManagementService.loadUser(member)).thenReturn(userInAServer);
        testUnit.execute(mockedMessage);
        verify(userExperienceService, times(1)).addExperience(userInAServer);
    }
}
