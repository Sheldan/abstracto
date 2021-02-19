package dev.sheldan.abstracto.utility.service;

import dev.sheldan.abstracto.core.exception.ChannelNotInGuildException;
import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.utility.config.posttargets.SuggestionPostTarget;
import dev.sheldan.abstracto.utility.exception.SuggestionNotFoundException;
import dev.sheldan.abstracto.utility.exception.SuggestionUpdateException;
import dev.sheldan.abstracto.utility.models.SuggestionState;
import dev.sheldan.abstracto.utility.models.database.Suggestion;
import dev.sheldan.abstracto.utility.models.template.commands.SuggestionLog;
import dev.sheldan.abstracto.utility.service.management.SuggestionManagementService;
import net.dv8tion.jda.api.entities.*;
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

import static dev.sheldan.abstracto.utility.service.SuggestionServiceBean.SUGGESTION_COUNTER_KEY;
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
    private ChannelService channelService;

    @Mock
    private MemberService memberService;

    @Mock
    private ReactionService reactionService;

    @Mock
    private SuggestionServiceBean self;

    @Mock
    private Member suggestionCreator;

    @Mock
    private Guild guild;

    @Mock
    private TextChannel textChannel;

    @Mock
    private CounterService counterService;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    @Mock
    private ServerManagementService serverManagementService;

    @Mock
    private AServer server;

    @Mock
    private AChannel channel;

    @Mock
    private AUserInAServer suggester;

    @Mock
    private AUser suggesterUser;

    @Mock
    private Member suggesterMember;

    private static final Long SUGGESTER_ID = 8L;
    private static final Long SERVER_ID = 3L;
    private static final Long CHANNEL_ID = 7L;
    private static final Long SUGGESTION_ID = 5L;

    @Test
    public void testCreateSuggestionMessage() {
        String suggestionText = "text";
        SuggestionLog log = Mockito.mock(SuggestionLog.class);
        when(suggestionCreator.getGuild()).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(serverManagementService.loadServer(suggestionCreator.getGuild())).thenReturn(server);
        MessageToSend messageToSend = MessageToSend.builder().build();
        when(templateService.renderEmbedTemplate(eq(SuggestionServiceBean.SUGGESTION_LOG_TEMPLATE), any(SuggestionLog.class))).thenReturn(messageToSend);
        Message suggestionMessage = Mockito.mock(Message.class);
        when(counterService.getNextCounterValue(server, SUGGESTION_COUNTER_KEY)).thenReturn(SUGGESTION_ID);
        AUserInAServer aUserInAServer = Mockito.mock(AUserInAServer.class);
        when(userInServerManagementService.loadOrCreateUser(suggestionCreator)).thenReturn(aUserInAServer);
        List<CompletableFuture<Message>> postingFutures = Arrays.asList(CompletableFuture.completedFuture(suggestionMessage));
        when(postTargetService.sendEmbedInPostTarget(messageToSend, SuggestionPostTarget.SUGGESTION, SERVER_ID)).thenReturn(postingFutures);
        testUnit.createSuggestionMessage(suggestionCreator, suggestionText, log);
        verify(reactionService, times(1)).addReactionToMessageAsync(SuggestionServiceBean.SUGGESTION_YES_EMOTE, SERVER_ID, suggestionMessage);
        verify(reactionService, times(1)).addReactionToMessageAsync(SuggestionServiceBean.SUGGESTION_NO_EMOTE, SERVER_ID, suggestionMessage);
    }

    @Test
    public void testCreateSuggestion() {
        when(suggesterMember.getGuild()).thenReturn(guild);
        when(guild.getId()).thenReturn("5");
        String text = "text";
        Message message = Mockito.mock(Message.class);
        testUnit.persistSuggestionInDatabase(suggesterMember, text, message, SUGGESTION_ID);
        verify(suggestionManagementService, times(1)).createSuggestion(suggesterMember, text, message, SUGGESTION_ID);
    }

    @Test
    public void testAcceptExistingSuggestion() {
        executeAcceptWithMember(suggesterMember);
    }

    @Test(expected = SuggestionNotFoundException.class)
    public void testAcceptNotExistingSuggestion() {
        when(suggestionManagementService.getSuggestion(SUGGESTION_ID, SERVER_ID)).thenReturn(Optional.empty());
        SuggestionLog log = Mockito.mock(SuggestionLog.class);
        when(log.getGuild()).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        testUnit.acceptSuggestion(SUGGESTION_ID, CLOSING_TEXT, log);
    }

    @Test
    public void testAcceptSuggestionWithMemberLeavingGuild() {
        executeAcceptWithMember(null);
    }

    @Test(expected = ChannelNotInGuildException.class)
    public void testAcceptSuggestionInNoTextChannel() {
        setupForNoTextChannel();
        SuggestionLog log = Mockito.mock(SuggestionLog.class);
        when(log.getGuild()).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        testUnit.acceptSuggestion(SUGGESTION_ID, CLOSING_TEXT, log);
    }

    private void setupForNoTextChannel() {
        Long messageId = 7L;
        Suggestion suggestionToAccept = Suggestion
                .builder()
                .channel(channel)
                .server(server)
                .suggestionId(new ServerSpecificId(server.getId(), SUGGESTION_ID))
                .suggester(suggester)
                .messageId(messageId)
                .build();
        when(server.getId()).thenReturn(SERVER_ID);
        when(channel.getId()).thenReturn(CHANNEL_ID);
        when(channelService.getTextChannelFromServer(SERVER_ID, CHANNEL_ID)).thenThrow(new ChannelNotInGuildException(CHANNEL_ID));
        when(suggestionManagementService.getSuggestion(SUGGESTION_ID, SERVER_ID)).thenReturn(Optional.of(suggestionToAccept));
    }

    @Test(expected = SuggestionUpdateException.class)
    public void testUpdateSuggestionTextWithoutEmbed() {
        SuggestionLog log = SuggestionLog.builder().build();
        Message suggestionMessage = Mockito.mock(Message.class);
        testUnit.updateSuggestionMessageText(CLOSING_TEXT, log, suggestionMessage);
    }

    @Test
    public void testUpdateSuggestionMessageWithEmbed() {
        SuggestionLog log = Mockito.mock(SuggestionLog.class);
        when(log.getGuild()).thenReturn(guild);
        MessageEmbed embed = Mockito.mock(MessageEmbed.class);
        when(embed.getDescription()).thenReturn("description");
        Message suggestionMessage = Mockito.mock(Message.class);
        when(suggestionMessage.getGuild()).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(suggestionMessage.getEmbeds()).thenReturn(Arrays.asList(embed));
        MessageToSend updatedMessage = MessageToSend.builder().build();
        when(templateService.renderEmbedTemplate(eq(SuggestionServiceBean.SUGGESTION_LOG_TEMPLATE), any(SuggestionLog.class))).thenReturn(updatedMessage);
        testUnit.updateSuggestionMessageText(CLOSING_TEXT, log, suggestionMessage);
        verify(postTargetService, times(1)).sendEmbedInPostTarget(updatedMessage, SuggestionPostTarget.SUGGESTION, SERVER_ID);
    }

    @Test
    public void testRejectExistingSuggestion() {
        executeRejectWithMember(suggesterMember);
    }

    @Test(expected = SuggestionNotFoundException.class)
    public void testRejectNotExistingSuggestion() {
        when(suggestionManagementService.getSuggestion(SUGGESTION_ID, SERVER_ID)).thenReturn(Optional.empty());
        SuggestionLog log = Mockito.mock(SuggestionLog.class);
        when(log.getGuild()).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        testUnit.rejectSuggestion(SUGGESTION_ID, CLOSING_TEXT, log);
    }

    @Test
    public void testRejectSuggestionWithMemberLeavingGuild() {
        executeRejectWithMember(null);
    }

    @Test(expected = ChannelNotInGuildException.class)
    public void testRejectSuggestionInNoTextChannel() {
        setupForNoTextChannel();
        SuggestionLog log = Mockito.mock(SuggestionLog.class);
        when(log.getGuild()).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        testUnit.rejectSuggestion(SUGGESTION_ID, CLOSING_TEXT, log);
    }

    private void executeAcceptWithMember(Member actualMember) {
        Long messageId = 7L;
        SuggestionLog log = Mockito.mock(SuggestionLog.class);
        when(log.getGuild()).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        Suggestion suggestionToAccept = setupClosing(messageId);
        Message suggestionMessage = Mockito.mock(Message.class);
        when(channelService.retrieveMessageInChannel(textChannel, messageId)).thenReturn(CompletableFuture.completedFuture(suggestionMessage));
        when(memberService.getMemberInServerAsync(SERVER_ID, SUGGESTER_ID)).thenReturn(CompletableFuture.completedFuture(actualMember));
        testUnit.acceptSuggestion(SUGGESTION_ID, CLOSING_TEXT, log);
        verify(suggestionManagementService, times(1)).setSuggestionState(suggestionToAccept, SuggestionState.ACCEPTED);
    }

    private void executeRejectWithMember(Member actualMember) {
        Long messageId = 7L;
        SuggestionLog log = Mockito.mock(SuggestionLog.class);
        when(log.getGuild()).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        Suggestion suggestionToAccept = setupClosing(messageId);
        Message suggestionMessage = Mockito.mock(Message.class);
        when(channelService.retrieveMessageInChannel(textChannel, messageId)).thenReturn(CompletableFuture.completedFuture(suggestionMessage));
        when(memberService.getMemberInServerAsync(SERVER_ID, SUGGESTER_ID)).thenReturn(CompletableFuture.completedFuture(actualMember));
        testUnit.rejectSuggestion(SUGGESTION_ID, CLOSING_TEXT, log);
        verify(suggestionManagementService, times(1)).setSuggestionState(suggestionToAccept, SuggestionState.REJECTED);
    }

    private Suggestion setupClosing(Long messageId) {
        Suggestion suggestionToAccept = Suggestion
                .builder()
                .channel(channel)
                .server(server)
                .suggestionId(new ServerSpecificId(server.getId(), SUGGESTION_ID))
                .suggester(suggester)
                .messageId(messageId)
                .build();
        when(server.getId()).thenReturn(SERVER_ID);
        when(channel.getId()).thenReturn(CHANNEL_ID);
        when(suggester.getUserReference()).thenReturn(suggesterUser);
        when(suggesterUser.getId()).thenReturn(SUGGESTER_ID);
        when(suggestionManagementService.getSuggestion(SUGGESTION_ID, SERVER_ID)).thenReturn(Optional.of(suggestionToAccept));
        when(channelService.getTextChannelFromServer(SERVER_ID, CHANNEL_ID)).thenReturn(textChannel);
        return suggestionToAccept;
    }
}
