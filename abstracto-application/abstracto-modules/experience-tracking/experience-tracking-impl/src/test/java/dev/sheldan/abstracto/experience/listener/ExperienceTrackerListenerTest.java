package dev.sheldan.abstracto.experience.listener;

import dev.sheldan.abstracto.core.models.cache.CachedAuthor;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.experience.service.AUserExperienceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ExperienceTrackerListenerTest {

    @InjectMocks
    public ExperienceTrackerListener testUnit;

    @Mock
    private AUserExperienceService userExperienceService;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    @Test
    public void testExperienceTracking() {
        AServer server = Mockito.mock(AServer.class);
        AUser user = Mockito.mock(AUser.class);
        AUserInAServer userInAServer = Mockito.mock(AUserInAServer.class);
        CachedMessage mockedMessage = Mockito.mock(CachedMessage.class);
        CachedAuthor cachedAuthor = Mockito.mock(CachedAuthor.class);
        when(mockedMessage.getAuthor()).thenReturn(cachedAuthor);
        when(userInServerManagementService.loadOrCreateUser(server.getId(), user.getId())).thenReturn(userInAServer);
        testUnit.execute(mockedMessage);
        verify(userExperienceService, times(1)).addExperience(userInAServer);
    }
}
