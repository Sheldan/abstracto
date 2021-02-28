package dev.sheldan.abstracto.utility.service.management;

import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.utility.models.SuggestionState;
import dev.sheldan.abstracto.utility.models.database.Suggestion;
import dev.sheldan.abstracto.utility.repository.SuggestionRepository;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SuggestionManagementServiceBeanTest {

    @InjectMocks
    private SuggestionManagementServiceBean testUnit;

    @Mock
    private SuggestionRepository suggestionRepository;

    @Mock
    private ChannelManagementService channelManagementService;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    @Mock
    private ServerManagementService serverManagementService;

    @Mock
    private AServer server;

    @Mock
    private AUserInAServer aUserInAServer;

    @Mock
    private AUser aUser;

    public static final long CHANNEL_ID = 6L;
    public static final long SERVER_ID = 6L;
    public static final long SUGGESTION_ID = 6L;

    @Test
    public void testCreateSuggestionViaUser() {
        String text = "text";
        when(aUserInAServer.getServerReference()).thenReturn(server);
        when(aUserInAServer.getUserReference()).thenReturn(aUser);
        Guild guild = Mockito.mock(Guild.class);
        Message message = Mockito.mock(Message.class);
        MessageChannel messageChannel = Mockito.mock(MessageChannel.class);
        when(messageChannel.getIdLong()).thenReturn(CHANNEL_ID);
        when(message.getChannel()).thenReturn(messageChannel);
        when(message.getGuild()).thenReturn(guild);
        when(guild.getId()).thenReturn("8");
        long suggestionId = 1L;
        Suggestion createdSuggestion = testUnit.createSuggestion(aUserInAServer, text, message, suggestionId);
        verify(suggestionRepository, times(1)).save(createdSuggestion);
        Assert.assertEquals(SuggestionState.NEW, createdSuggestion.getState());
        Assert.assertEquals(aUserInAServer, createdSuggestion.getSuggester());
        Assert.assertEquals(server, createdSuggestion.getServer());
    }

    @Test
    public void testCreateSuggestionViaMember() {
        Member member = Mockito.mock(Member.class);
        String text = "text";
        Guild guild = Mockito.mock(Guild.class);
        Message message = Mockito.mock(Message.class);
        MessageChannel messageChannel = Mockito.mock(MessageChannel.class);
        when(messageChannel.getIdLong()).thenReturn(CHANNEL_ID);
        when(aUserInAServer.getServerReference()).thenReturn(server);
        when(aUserInAServer.getUserReference()).thenReturn(aUser);
        when(message.getChannel()).thenReturn(messageChannel);
        when(message.getGuild()).thenReturn(guild);
        when(guild.getId()).thenReturn("5");
        when(userInServerManagementService.loadOrCreateUser(member)).thenReturn(aUserInAServer);
        long suggestionId = 1L;
        Suggestion createdSuggestion = testUnit.createSuggestion(member, text, message, suggestionId);
        verify(suggestionRepository, times(1)).save(createdSuggestion);
        Assert.assertEquals(SuggestionState.NEW, createdSuggestion.getState());
        Assert.assertEquals(aUserInAServer, createdSuggestion.getSuggester());
        Assert.assertEquals(server, createdSuggestion.getServer());
    }

    @Test
    public void testGetSuggestion() {
        Suggestion foundSuggestion = createSuggestion();
        when(suggestionRepository.findById(new ServerSpecificId(SERVER_ID, SUGGESTION_ID))).thenReturn(Optional.of(foundSuggestion));
        Optional<Suggestion> suggestionOptional = testUnit.getSuggestion(SUGGESTION_ID, SERVER_ID);
        Assert.assertTrue(suggestionOptional.isPresent());
        suggestionOptional.ifPresent(suggestion -> Assert.assertEquals(SUGGESTION_ID, suggestion.getSuggestionId().getId().longValue()));
    }

    @Test
    public void testGetSuggestionNotFound() {
        when(suggestionRepository.findById(new ServerSpecificId(SERVER_ID, SUGGESTION_ID))).thenReturn(Optional.empty());
        Optional<Suggestion> suggestionOptional = testUnit.getSuggestion(SUGGESTION_ID, SERVER_ID);
        Assert.assertFalse(suggestionOptional.isPresent());
    }


    @Test
    public void setSuggestionState() {
        Suggestion suggestion = createSuggestion();
        testUnit.setSuggestionState(suggestion, SuggestionState.ACCEPTED);
        verify(suggestion, times(1)).setState(SuggestionState.ACCEPTED);
        verify(suggestionRepository, times(1)).save(suggestion);
    }


    private Suggestion createSuggestion() {
        Suggestion foundSuggestion = Mockito.mock(Suggestion.class);
        ServerSpecificId suggestionId = Mockito.mock(ServerSpecificId.class);
        when(suggestionId.getId()).thenReturn(SUGGESTION_ID);
        when(suggestionId.getServerId()).thenReturn(SERVER_ID);
        when(foundSuggestion.getSuggestionId()).thenReturn(suggestionId);
        return foundSuggestion;
    }
}
