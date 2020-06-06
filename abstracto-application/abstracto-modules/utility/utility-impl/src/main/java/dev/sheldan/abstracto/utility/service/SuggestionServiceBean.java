package dev.sheldan.abstracto.utility.service;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.utils.MessageUtils;
import dev.sheldan.abstracto.templating.service.TemplateService;
import dev.sheldan.abstracto.utility.config.posttargets.SuggestionPostTarget;
import dev.sheldan.abstracto.utility.exception.SuggestionNotFoundException;
import dev.sheldan.abstracto.utility.exception.SuggestionUpdateException;
import dev.sheldan.abstracto.utility.models.database.Suggestion;
import dev.sheldan.abstracto.utility.models.SuggestionState;
import dev.sheldan.abstracto.utility.models.template.commands.SuggestionLog;
import dev.sheldan.abstracto.utility.service.management.SuggestionManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
public class SuggestionServiceBean implements SuggestionService {

    public static final String SUGGESTION_LOG_TEMPLATE = "suggest_log";
    public static final String SUGGESTION_YES_EMOTE = "suggestionYes";
    public static final String SUGGESTION_NO_EMOTE = "suggestionNo";

    @Autowired
    private SuggestionManagementService suggestionManagementService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private BotService botService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private SuggestionServiceBean self;

    @Override
    public void createSuggestion(Member member, String text, SuggestionLog suggestionLog)  {
        Suggestion suggestion = suggestionManagementService.createSuggestion(member, text);
        suggestionLog.setSuggestion(suggestion);
        suggestionLog.setSuggesterUser(suggestion.getSuggester());
        suggestionLog.setText(text);
        Long suggestionId = suggestion.getId();
        MessageToSend messageToSend = templateService.renderEmbedTemplate(SUGGESTION_LOG_TEMPLATE, suggestionLog);
        long guildId = member.getGuild().getIdLong();
        List<CompletableFuture<Message>> completableFutures = postTargetService.sendEmbedInPostTarget(messageToSend, SuggestionPostTarget.SUGGESTION, guildId);
        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).thenAccept(aVoid -> {
            Suggestion innerSuggestion = suggestionManagementService.getSuggestion(suggestionId).orElseThrow(() -> new SuggestionNotFoundException(suggestionId));
            try {
                Message message = completableFutures.get(0).get();
                suggestionManagementService.setPostedMessage(innerSuggestion, message);
                messageService.addReactionToMessage(SUGGESTION_YES_EMOTE, guildId, message);
                messageService.addReactionToMessage(SUGGESTION_NO_EMOTE, guildId, message);
            } catch (InterruptedException | ExecutionException e) {
                log.warn("Failed to post suggestion", e);
                Thread.currentThread().interrupt();
            }
        }) .exceptionally(throwable -> {
            log.error("Failed to post suggestion {}", suggestionId, throwable);
            return null;
        });
    }

    @Override
    public void acceptSuggestion(Long suggestionId, String text, SuggestionLog suggestionLog) {
        Suggestion suggestion = suggestionManagementService.getSuggestion(suggestionId).orElseThrow(() -> new SuggestionNotFoundException(suggestionId));
        suggestionManagementService.setSuggestionState(suggestion, SuggestionState.ACCEPTED);
        updateSuggestion(text, suggestionLog, suggestion);
    }

    private void updateSuggestion(String text, SuggestionLog suggestionLog, Suggestion suggestion) {
        suggestionLog.setSuggesterUser(suggestion.getSuggester());
        Long channelId = suggestion.getChannel().getId();
        Long originalMessageId = suggestion.getMessageId();
        Long serverId = suggestion.getServer().getId();

        suggestionLog.setOriginalChannelId(channelId);
        suggestionLog.setOriginalMessageId(originalMessageId);
        suggestionLog.setOriginalMessageUrl(MessageUtils.buildMessageUrl(serverId, channelId, originalMessageId));
        AUserInAServer suggester = suggestion.getSuggester();
        Optional<Guild> guildByIdOptional = botService.getGuildById(serverId);
        if(guildByIdOptional.isPresent()) {
            Guild guildById = guildByIdOptional.get();
            Member memberById = guildById.getMemberById(suggester.getUserReference().getId());
            suggestionLog.setSuggester(memberById);
            suggestionLog.setSuggestion(suggestion);
            TextChannel textChannelById = guildById.getTextChannelById(channelId);
            if(textChannelById != null) {
                textChannelById.retrieveMessageById(originalMessageId).queue(message ->
                    self.updateSuggestionMessageText(text, suggestionLog, message)
                );
            } else {
                log.warn("Not possible to update suggestion {}, because text channel {} was not found in guild {}.", suggestion.getId(), channelId, serverId);
            }
        } else {
            log.warn("Not possible to update suggestion {}, because guild {} was not found.", suggestion.getId(), serverId);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateSuggestionMessageText(String text, SuggestionLog suggestionLog, Message message)  {
        Optional<MessageEmbed> embedOptional = message.getEmbeds().stream().filter(embed -> embed.getDescription() != null).findFirst();
        if(embedOptional.isPresent()) {
            MessageEmbed suggestionEmbed = embedOptional.get();
            suggestionLog.setReason(text);
            suggestionLog.setText(suggestionEmbed.getDescription());
            MessageToSend messageToSend = templateService.renderEmbedTemplate(SUGGESTION_LOG_TEMPLATE, suggestionLog);
            postTargetService.sendEmbedInPostTarget(messageToSend, SuggestionPostTarget.SUGGESTION, suggestionLog.getServer().getId());
        } else {
            log.warn("The message to update the suggestion for, did not contain an embed to update. Suggestions require an embed with a description as a container. MessageURL: {}", message.getJumpUrl());
            throw new SuggestionUpdateException("Not possible to update suggestion.");
        }
    }

    @Override
    public void rejectSuggestion(Long suggestionId, String text, SuggestionLog log) {
        Suggestion suggestion = suggestionManagementService.getSuggestion(suggestionId).orElseThrow(() ->  new SuggestionNotFoundException(suggestionId));
        suggestionManagementService.setSuggestionState(suggestion, SuggestionState.REJECTED);
        updateSuggestion(text, log, suggestion);
    }
}
