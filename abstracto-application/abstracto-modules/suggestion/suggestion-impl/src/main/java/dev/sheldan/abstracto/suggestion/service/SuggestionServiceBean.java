package dev.sheldan.abstracto.suggestion.service;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.core.utils.MessageUtils;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.suggestion.config.SuggestionPostTarget;
import dev.sheldan.abstracto.suggestion.exception.SuggestionNotFoundException;
import dev.sheldan.abstracto.suggestion.exception.SuggestionUpdateException;
import dev.sheldan.abstracto.suggestion.model.database.Suggestion;
import dev.sheldan.abstracto.suggestion.model.database.SuggestionState;
import dev.sheldan.abstracto.suggestion.model.template.SuggestionLog;
import dev.sheldan.abstracto.suggestion.service.management.SuggestionManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class SuggestionServiceBean implements SuggestionService {

    public static final String SUGGESTION_LOG_TEMPLATE = "suggest_log";
    public static final String SUGGESTION_YES_EMOTE = "suggestionYes";
    public static final String SUGGESTION_NO_EMOTE = "suggestionNo";
    public static final String SUGGESTION_COUNTER_KEY = "suggestion";

    @Autowired
    private SuggestionManagementService suggestionManagementService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private ReactionService reactionService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private SuggestionServiceBean self;

    @Autowired
    private CounterService counterService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public CompletableFuture<Void> createSuggestionMessage(Member member, String text, SuggestionLog suggestionLog)  {
        AServer server = serverManagementService.loadServer(member.getGuild());
        AUserInAServer suggester = userInServerManagementService.loadOrCreateUser(member);
        Long newSuggestionId = counterService.getNextCounterValue(server, SUGGESTION_COUNTER_KEY);
        suggestionLog.setSuggestionId(newSuggestionId);
        suggestionLog.setState(SuggestionState.NEW);
        suggestionLog.setSuggesterUser(suggester);
        suggestionLog.setText(text);
        MessageToSend messageToSend = templateService.renderEmbedTemplate(SUGGESTION_LOG_TEMPLATE, suggestionLog);
        long guildId = member.getGuild().getIdLong();
        log.info("Creating suggestion with id {} in server {} from member {}.", newSuggestionId, member.getGuild().getId(), member.getId());
        List<CompletableFuture<Message>> completableFutures = postTargetService.sendEmbedInPostTarget(messageToSend, SuggestionPostTarget.SUGGESTION, guildId);
        return FutureUtils.toSingleFutureGeneric(completableFutures).thenCompose(aVoid -> {
            Message message = completableFutures.get(0).join();
            log.trace("Posted message, adding reaction for suggestion {} to message {}.", newSuggestionId, message.getId());
            CompletableFuture<Void> firstReaction = reactionService.addReactionToMessageAsync(SUGGESTION_YES_EMOTE, guildId, message);
            CompletableFuture<Void> secondReaction = reactionService.addReactionToMessageAsync(SUGGESTION_NO_EMOTE, guildId, message);
            return CompletableFuture.allOf(firstReaction, secondReaction).thenAccept(aVoid1 -> {
                log.trace("Reaction added to message {} for suggestion {}.", message.getId(), newSuggestionId);
                self.persistSuggestionInDatabase(member, text, message, newSuggestionId);
            });
        });
    }

    @Transactional
    public void persistSuggestionInDatabase(Member member, String text, Message message, Long suggestionId) {
        log.info("Persisting suggestion {} for server {} in database.", suggestionId, member.getGuild().getId());
        suggestionManagementService.createSuggestion(member, text, message, suggestionId);
    }

    @Override
    public CompletableFuture<Void> acceptSuggestion(Long suggestionId, String text, SuggestionLog suggestionLog) {
        Suggestion suggestion = suggestionManagementService.getSuggestion(suggestionId, suggestionLog.getGuild().getIdLong()).orElseThrow(() -> new SuggestionNotFoundException(suggestionId));
        suggestionManagementService.setSuggestionState(suggestion, SuggestionState.ACCEPTED);
        log.info("Accepting suggestion {} in server {}.", suggestionId, suggestion.getServer().getId());
        return updateSuggestion(text, suggestionLog, suggestion);
    }

    private CompletableFuture<Void> updateSuggestion(String text, SuggestionLog suggestionLog, Suggestion suggestion) {
        suggestionLog.setSuggesterUser(suggestion.getSuggester());
        Long channelId = suggestion.getChannel().getId();
        Long originalMessageId = suggestion.getMessageId();
        Long serverId = suggestion.getServer().getId();
        log.info("Updated posted suggestion {} in server {}.", suggestion.getSuggestionId().getId(), suggestion.getServer().getId());

        suggestionLog.setOriginalChannelId(channelId);
        suggestionLog.setOriginalMessageId(originalMessageId);
        suggestionLog.setOriginalMessageUrl(MessageUtils.buildMessageUrl(serverId, channelId, originalMessageId));
        AUserInAServer suggester = suggestion.getSuggester();
        TextChannel textChannelById = channelService.getTextChannelFromServer(serverId, channelId);
        CompletableFuture<Member> memberById = memberService.getMemberInServerAsync(serverId, suggester.getUserReference().getId());
        suggestionLog.setState(suggestion.getState());
        suggestionLog.setSuggestionId(suggestion.getSuggestionId().getId());
        CompletableFuture<Void> finalFuture = new CompletableFuture<>();
        memberById.whenComplete((member, throwable) -> {
            if(throwable == null) {
                suggestionLog.setSuggester(member);
            }
            channelService.retrieveMessageInChannel(textChannelById, originalMessageId).thenCompose(message ->
                self.updateSuggestionMessageText(text, suggestionLog, message)
            ).thenAccept(aVoid ->  finalFuture.complete(null));
        });

        return finalFuture;

    }

    @Transactional
    public CompletableFuture<Void> updateSuggestionMessageText(String text, SuggestionLog suggestionLog, Message message)  {
        Optional<MessageEmbed> embedOptional = message.getEmbeds().stream().filter(embed -> embed.getDescription() != null).findFirst();
        if(embedOptional.isPresent()) {
            log.info("Updating the text of the suggestion {} in server {}.", suggestionLog.getSuggestionId(), message.getGuild().getId());
            MessageEmbed suggestionEmbed = embedOptional.get();
            suggestionLog.setReason(text);
            suggestionLog.setText(suggestionEmbed.getDescription());
            MessageToSend messageToSend = templateService.renderEmbedTemplate(SUGGESTION_LOG_TEMPLATE, suggestionLog);
            List<CompletableFuture<Message>> completableFutures = postTargetService.sendEmbedInPostTarget(messageToSend, SuggestionPostTarget.SUGGESTION, suggestionLog.getGuild().getIdLong());
            return CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]));
        } else {
            log.warn("The message to update the suggestion for, did not contain an embed to update. Suggestions require an embed with a description as a container. MessageURL: {}", message.getJumpUrl());
            throw new SuggestionUpdateException();
        }
    }

    @Override
    public CompletableFuture<Void> rejectSuggestion(Long suggestionId, String text, SuggestionLog suggestionLog) {
        Suggestion suggestion = suggestionManagementService.getSuggestion(suggestionId, suggestionLog.getGuild().getIdLong()).orElseThrow(() ->  new SuggestionNotFoundException(suggestionId));
        suggestionManagementService.setSuggestionState(suggestion, SuggestionState.REJECTED);
        log.info("Rejecting suggestion {} in server {}.", suggestionId, suggestion.getServer().getId());
        return updateSuggestion(text, suggestionLog, suggestion);
    }
}
