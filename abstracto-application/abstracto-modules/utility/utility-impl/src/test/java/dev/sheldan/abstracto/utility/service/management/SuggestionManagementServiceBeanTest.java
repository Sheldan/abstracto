package dev.sheldan.abstracto.utility.service.management;

import dev.sheldan.abstracto.core.exception.ChannelNotFoundException;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.test.MockUtils;
import dev.sheldan.abstracto.utility.models.SuggestionState;
import dev.sheldan.abstracto.utility.models.database.Suggestion;
import dev.sheldan.abstracto.utility.repository.SuggestionRepository;
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

    @Test
    public void testCreateSuggestionViaUser() {
        AServer server = MockUtils.getServer();
        AUserInAServer userInAServer = MockUtils.getUserObject(5L, server);
        String text = "text";
        Suggestion createdSuggestion = testUnit.createSuggestion(userInAServer, text);
        verify(suggestionRepository, times(1)).save(createdSuggestion);
        Assert.assertEquals(SuggestionState.NEW, createdSuggestion.getState());
        Assert.assertEquals(userInAServer.getUserInServerId(), createdSuggestion.getSuggester().getUserInServerId());
        Assert.assertEquals(server.getId(), createdSuggestion.getServer().getId());
    }

    @Test
    public void testCreateSuggestionViaMember() {
        Member member = Mockito.mock(Member.class);
        AServer server = MockUtils.getServer();
        AUserInAServer userInAServer = MockUtils.getUserObject(5L, server);
        String text = "text";
        when(userInServerManagementService.loadUser(member)).thenReturn(userInAServer);
        Suggestion createdSuggestion = testUnit.createSuggestion(member, text);
        verify(suggestionRepository, times(1)).save(createdSuggestion);
        Assert.assertEquals(SuggestionState.NEW, createdSuggestion.getState());
        Assert.assertEquals(userInAServer.getUserInServerId(), createdSuggestion.getSuggester().getUserInServerId());
        Assert.assertEquals(server.getId(), createdSuggestion.getServer().getId());
    }

    @Test
    public void testGetSuggestion() {
        Long suggestionId = 5L;
        Suggestion foundSuggestion = Suggestion.builder().id(suggestionId).build();
        when(suggestionRepository.findById(suggestionId)).thenReturn(Optional.of(foundSuggestion));
        Optional<Suggestion> suggestionOptional = testUnit.getSuggestion(suggestionId);
        Assert.assertTrue(suggestionOptional.isPresent());
        suggestionOptional.ifPresent(suggestion -> Assert.assertEquals(suggestionId, suggestion.getId()));
    }

    @Test
    public void testGetSuggestionNotFound() {
        Long suggestionId = 5L;
        when(suggestionRepository.findById(suggestionId)).thenReturn(Optional.empty());
        Optional<Suggestion> suggestionOptional = testUnit.getSuggestion(suggestionId);
        Assert.assertFalse(suggestionOptional.isPresent());
    }

    @Test
    public void testSetPostedMessage() {
        Long messageId = 5L;
        Long channelId = 6L;
        Suggestion suggestion = Suggestion.builder().build();
        Message message = Mockito.mock(Message.class);
        MessageChannel channel = Mockito.mock(MessageChannel.class);
        when(message.getChannel()).thenReturn(channel);
        when(channel.getIdLong()).thenReturn(channelId);
        when(message.getIdLong()).thenReturn(messageId);
        AChannel aChannel = AChannel.builder().id(channelId).build();
        when(channelManagementService.loadChannel(channelId)).thenReturn(Optional.of(aChannel));
        testUnit.setPostedMessage(suggestion, message);
        Assert.assertEquals(messageId, suggestion.getMessageId());
        Assert.assertEquals(channelId, suggestion.getChannel().getId());
        verify(suggestionRepository, times(1)).save(suggestion);
    }

    @Test(expected = ChannelNotFoundException.class)
    public void testSetPostedMessageChannelNotFound() {
        Long channelId = 6L;
        AServer server = MockUtils.getServer();
        Suggestion suggestion = Suggestion.builder().server(server).build();
        Message message = Mockito.mock(Message.class);
        MessageChannel channel = Mockito.mock(MessageChannel.class);
        when(message.getChannel()).thenReturn(channel);
        when(channel.getIdLong()).thenReturn(channelId);
        when(channelManagementService.loadChannel(channelId)).thenReturn(Optional.empty());
        testUnit.setPostedMessage(suggestion, message);
    }

    @Test
    public void setSuggestionState() {
        Suggestion suggestion = Suggestion.builder().build();
        testUnit.setSuggestionState(suggestion, SuggestionState.ACCEPTED);
        Assert.assertEquals(suggestion.getState(), SuggestionState.ACCEPTED);
        verify(suggestionRepository, times(1)).save(suggestion);
    }
}
