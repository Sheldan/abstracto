package dev.sheldan.abstracto.experience.listener;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.listener.MessageReceivedModel;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.experience.service.AUserExperienceService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
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

    @Mock
    private MessageReceivedModel model;

    @Mock
    private User user;

    private static final Long SERVER_ID = 4L;
    private static final Long USER_ID = 5L;

    @Test
    public void testExperienceTracking() {
        AUserInAServer userInAServer = Mockito.mock(AUserInAServer.class);
        Message mockedMessage = Mockito.mock(Message.class);
        when(mockedMessage.isFromGuild()).thenReturn(true);
        when(mockedMessage.isWebhookMessage()).thenReturn(false);
        MessageChannelUnion channel = Mockito.mock(MessageChannelUnion.class);
        MessageType type = MessageType.DEFAULT;
        when(mockedMessage.getType()).thenReturn(type);
        when(userInServerManagementService.loadOrCreateUser(SERVER_ID, USER_ID)).thenReturn(userInAServer);
        when(model.getMessage()).thenReturn(mockedMessage);
        when(userExperienceService.experienceGainEnabledInChannel(channel)).thenReturn(true);
        when(mockedMessage.getChannel()).thenReturn(channel);
        when(model.getServerId()).thenReturn(SERVER_ID);
        when(mockedMessage.getAuthor()).thenReturn(user);
        when(user.getIdLong()).thenReturn(USER_ID);
        testUnit.execute(model);
        verify(userExperienceService, times(1)).addExperience(userInAServer);
    }
}
