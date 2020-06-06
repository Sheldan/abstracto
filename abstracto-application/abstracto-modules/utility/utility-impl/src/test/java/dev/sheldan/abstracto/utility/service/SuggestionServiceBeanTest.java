package dev.sheldan.abstracto.utility.service;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import dev.sheldan.abstracto.test.MockUtils;
import dev.sheldan.abstracto.utility.config.posttargets.SuggestionPostTarget;
import dev.sheldan.abstracto.utility.exception.SuggestionNotFoundException;
import dev.sheldan.abstracto.utility.exception.SuggestionUpdateException;
import dev.sheldan.abstracto.utility.models.SuggestionState;
import dev.sheldan.abstracto.utility.models.database.Suggestion;
import dev.sheldan.abstracto.utility.models.template.commands.SuggestionLog;
import dev.sheldan.abstracto.utility.service.management.SuggestionManagementService;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.RestAction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SuggestionServiceBeanTest {

    public static final String CLOSING_TEXT = "accepted";

    @InjectMocks
    private SuggestionServiceBean testUnit;

    @Mock
    private SuggestionManagementService suggestionManagementService;

    @Mock
    private PostTargetService postTargetService;

    @Mock
    private TemplateService templateService;

    @Mock
    private BotService botService;

    @Mock
    private MessageService messageService;

    @Mock
    private SuggestionServiceBean self;

    @Mock
    private Member suggestionCreator;

    @Mock
    private Guild guild;

    @Mock
    private TextChannel textChannel;

    @Test
    public void testCreateSuggestion() {
        String suggestionText = "text";
        AServer server = MockUtils.getServer();
        SuggestionLog log = SuggestionLog.builder().build();
        Long suggestionId = 5L;
        Suggestion createdSuggestion = Suggestion.builder().id(suggestionId).build();
        when(suggestionCreator.getGuild()).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(server.getId());
        when(suggestionManagementService.createSuggestion(suggestionCreator, suggestionText)).thenReturn(createdSuggestion);
        MessageToSend messageToSend = MessageToSend.builder().build();
        when(templateService.renderEmbedTemplate(eq(SuggestionServiceBean.SUGGESTION_LOG_TEMPLATE), any(SuggestionLog.class))).thenReturn(messageToSend);
        Message suggestionMessage = Mockito.mock(Message.class);
        List<CompletableFuture<Message>> postingFutures = Arrays.asList(CompletableFuture.completedFuture(suggestionMessage));
        when(postTargetService.sendEmbedInPostTarget(messageToSend, SuggestionPostTarget.SUGGESTION, server.getId())).thenReturn(postingFutures);
        when(suggestionManagementService.getSuggestion(suggestionId)).thenReturn(Optional.of(createdSuggestion));
        testUnit.createSuggestion(suggestionCreator, suggestionText, log);
        verify(suggestionManagementService, times(1)).setPostedMessage(createdSuggestion, suggestionMessage);
        verify( messageService, times(1)).addReactionToMessage(SuggestionServiceBean.SUGGESTION_YES_EMOTE, server.getId(), suggestionMessage);
        verify( messageService, times(1)).addReactionToMessage(SuggestionServiceBean.SUGGESTION_NO_EMOTE, server.getId(), suggestionMessage);
    }

    @Test
    public void testAcceptExistingSuggestion() {
        Member suggesterMember = Mockito.mock(Member.class);
        executeAcceptWithMember(suggesterMember);
    }

    @Test(expected = SuggestionNotFoundException.class)
    public void testAcceptNotExistingSuggestion() {
        Long suggestionId = 5L;
        when(suggestionManagementService.getSuggestion(suggestionId)).thenReturn(Optional.empty());
        testUnit.acceptSuggestion(suggestionId, CLOSING_TEXT, SuggestionLog.builder().build());
    }

    @Test
    public void testAcceptSuggestionWithMemberLeavingGuild() {
        executeAcceptWithMember(null);
    }

    @Test
    public void testAcceptSuggestionInNoTextChannel() {
        Long suggestionId = 5L;
        setupForNoTextChannel(suggestionId);
        testUnit.acceptSuggestion(suggestionId, CLOSING_TEXT, SuggestionLog.builder().build());
    }

    private void setupForNoTextChannel(Long suggestionId) {
        AServer server = MockUtils.getServer();
        Long channelId = 5L;
        Long messageId = 7L;
        AChannel channel = MockUtils.getTextChannel(server, channelId);
        AUserInAServer suggester = MockUtils.getUserObject(4L, server);
        Suggestion suggestionToAccept = Suggestion
                .builder()
                .channel(channel)
                .server(server)
                .id(suggestionId)
                .suggester(suggester)
                .messageId(messageId)
                .build();

        when(suggestionManagementService.getSuggestion(suggestionId)).thenReturn(Optional.of(suggestionToAccept));
        when(botService.getGuildById(server.getId())).thenReturn(Optional.of(guild));
        Member suggesterMember = Mockito.mock(Member.class);
        when(guild.getMemberById(suggester.getUserReference().getId())).thenReturn(suggesterMember);
        when(guild.getTextChannelById(channelId)).thenReturn(null);
    }

    @Test
    public void testAcceptSuggestionInNoGuild() {
        Long suggestionId = 5L;
        setupForNoGuild(suggestionId);
        testUnit.acceptSuggestion(suggestionId, CLOSING_TEXT, SuggestionLog.builder().build());
    }

    @Test(expected = SuggestionUpdateException.class)
    public void testUpdateSuggestionTextWithoutEmbed() {
        SuggestionLog log = SuggestionLog.builder().build();
        Message suggestionMessage = Mockito.mock(Message.class);
        testUnit.updateSuggestionMessageText(CLOSING_TEXT, log, suggestionMessage);
    }

    @Test
    public void testUpdateSuggestionMessageWithEmbed() {
        AServer server = MockUtils.getServer();
        SuggestionLog log = SuggestionLog.builder().server(server).build();
        MessageEmbed embed = Mockito.mock(MessageEmbed.class);
        when(embed.getDescription()).thenReturn("description");
        Message suggestionMessage = Mockito.mock(Message.class);
        when(suggestionMessage.getEmbeds()).thenReturn(Arrays.asList(embed));
        MessageToSend updatedMessage = MessageToSend.builder().build();
        when(templateService.renderEmbedTemplate(eq(SuggestionServiceBean.SUGGESTION_LOG_TEMPLATE), any(SuggestionLog.class))).thenReturn(updatedMessage);
        testUnit.updateSuggestionMessageText(CLOSING_TEXT, log, suggestionMessage);
        verify(postTargetService, times(1)).sendEmbedInPostTarget(updatedMessage, SuggestionPostTarget.SUGGESTION, server.getId());
    }

    @Test
    public void testRejectExistingSuggestion() {
        Member suggesterMember = Mockito.mock(Member.class);
        executeRejectWithMember(suggesterMember);
    }

    @Test(expected = SuggestionNotFoundException.class)
    public void testRejectNotExistingSuggestion() {
        Long suggestionId = 5L;
        when(suggestionManagementService.getSuggestion(suggestionId)).thenReturn(Optional.empty());
        testUnit.rejectSuggestion(suggestionId, CLOSING_TEXT, SuggestionLog.builder().build());
    }

    @Test
    public void testRejectSuggestionWithMemberLeavingGuild() {
        executeRejectWithMember(null);
    }

    @Test
    public void testRejectSuggestionInNoTextChannel() {
        Long suggestionId = setupForNoTextChannel();
        testUnit.rejectSuggestion(suggestionId, CLOSING_TEXT, SuggestionLog.builder().build());
    }

    @Test
    public void testRejectSuggestionInNoGuild() {
        Long suggestionId = 5L;
        setupForNoGuild(suggestionId);
        testUnit.rejectSuggestion(suggestionId, CLOSING_TEXT, SuggestionLog.builder().build());
    }

    private Long setupForNoTextChannel() {
        Long suggestionId = 5L;
        setupForNoTextChannel(suggestionId);
        return suggestionId;
    }

    private Long setupForNoGuild(Long suggestionId) {
        AServer server = MockUtils.getServer();
        Long channelId = 5L;
        AChannel channel = MockUtils.getTextChannel(server, channelId);
        Suggestion suggestionToAccept = Suggestion
                .builder()
                .server(server)
                .id(suggestionId)
                .channel(channel)
                .build();
        when(suggestionManagementService.getSuggestion(suggestionId)).thenReturn(Optional.of(suggestionToAccept));
        when(botService.getGuildById(server.getId())).thenReturn(Optional.empty());
        return suggestionId;
    }

    private void executeAcceptWithMember(Member suggesterMember) {
        Long suggestionId = 5L;
        Long channelId = 5L;
        Long messageId = 7L;
        SuggestionLog logParameter = SuggestionLog.builder().build();
        Suggestion suggestionToAccept = setupClosing(suggesterMember, suggestionId, channelId, messageId);
        RestAction<Message> retrievalAction = Mockito.mock(RestAction.class);
        when(textChannel.retrieveMessageById(messageId)).thenReturn(retrievalAction);
        testUnit.acceptSuggestion(suggestionId, CLOSING_TEXT, logParameter);
        verify(suggestionManagementService, times(1)).setSuggestionState(suggestionToAccept, SuggestionState.ACCEPTED);
        verify(retrievalAction, times(1)).queue(any(Consumer.class));
    }

    private void executeRejectWithMember(Member suggesterMember) {
        Long suggestionId = 5L;
        Long channelId = 5L;
        Long messageId = 7L;
        SuggestionLog logParameter = SuggestionLog.builder().build();
        Suggestion suggestionToAccept = setupClosing(suggesterMember, suggestionId, channelId, messageId);
        RestAction<Message> retrievalAction = Mockito.mock(RestAction.class);
        when(textChannel.retrieveMessageById(messageId)).thenReturn(retrievalAction);
        testUnit.rejectSuggestion(suggestionId, CLOSING_TEXT, logParameter);
        verify(suggestionManagementService, times(1)).setSuggestionState(suggestionToAccept, SuggestionState.REJECTED);
        verify(retrievalAction, times(1)).queue(any(Consumer.class));
    }

    private Suggestion setupClosing(Member suggesterMember, Long suggestionId, Long channelId, Long messageId) {
        AServer server = MockUtils.getServer();
        AChannel channel = MockUtils.getTextChannel(server, channelId);
        AUserInAServer suggester = MockUtils.getUserObject(4L, server);
        Suggestion suggestionToAccept = Suggestion
                .builder()
                .channel(channel)
                .server(server)
                .id(suggestionId)
                .suggester(suggester)
                .messageId(messageId)
                .build();
        when(suggestionManagementService.getSuggestion(suggestionId)).thenReturn(Optional.of(suggestionToAccept));
        when(botService.getGuildById(server.getId())).thenReturn(Optional.of(guild));
        when(guild.getTextChannelById(channelId)).thenReturn(textChannel);
        when(guild.getMemberById(suggester.getUserReference().getId())).thenReturn(suggesterMember);
        return suggestionToAccept;
    }
}
