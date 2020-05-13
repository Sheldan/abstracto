package dev.sheldan.abstracto.utility.service;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.utils.MessageUtils;
import dev.sheldan.abstracto.templating.service.TemplateService;
import dev.sheldan.abstracto.utility.exception.SuggestionNotFoundException;
import dev.sheldan.abstracto.utility.models.database.Suggestion;
import dev.sheldan.abstracto.utility.models.SuggestionState;
import dev.sheldan.abstracto.utility.models.template.commands.SuggestionLog;
import dev.sheldan.abstracto.utility.service.management.SuggestionManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
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
    private static final String SUGGESTION_YES_EMOTE = "suggestionYes";
    private static final String SUGGESTION_NO_EMOTE = "suggestionNo";
    public static final String SUGGESTIONS_TARGET = "suggestions";

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
    private SuggestionServiceBean self;

    @Override
    public void createSuggestion(Member member, String text, SuggestionLog suggestionLog)  {
        Suggestion suggestion = suggestionManagementService.createSuggestion(member, text);
        suggestionLog.setSuggestion(suggestion);
        suggestionLog.setText(text);
        Long suggestionId = suggestion.getId();
        MessageToSend messageToSend = templateService.renderEmbedTemplate(SUGGESTION_LOG_TEMPLATE, suggestionLog);
        long guildId = member.getGuild().getIdLong();
        JDA instance = botService.getInstance();
        Guild guildById = instance.getGuildById(guildId);
        if(guildById != null) {
            List<CompletableFuture<Message>> completableFutures = postTargetService.sendEmbedInPostTarget(messageToSend, SUGGESTIONS_TARGET, guildId);
            CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).thenAccept(aVoid -> {
                Suggestion innerSuggestion = suggestionManagementService.getSuggestion(suggestionId).orElseThrow(() -> new SuggestionNotFoundException(suggestionId));
                try {
                    Message message = completableFutures.get(0).get();
                    suggestionManagementService.setPostedMessage(innerSuggestion, message);
                    messageService.addReactionToMessage(SUGGESTION_YES_EMOTE, guildId, message);
                    messageService.addReactionToMessage(SUGGESTION_NO_EMOTE, guildId, message);
                } catch (InterruptedException | ExecutionException e) {
                    log.warn("Failed to post suggestion", e);
                }
            }) .exceptionally(throwable -> {
                log.error("Failed to post suggestion {}", suggestionId, throwable);
                return null;
            });
        } else {
            log.warn("Guild {} or member {} was not found when creating suggestion.", member.getGuild().getIdLong(), member.getIdLong());
        }
    }

    @Override
    public void acceptSuggestion(Long suggestionId, String text, SuggestionLog suggestionLog) {
        Suggestion suggestion = suggestionManagementService.getSuggestion(suggestionId).orElseThrow(() -> new SuggestionNotFoundException(suggestionId));
        suggestionManagementService.setSuggestionState(suggestion, SuggestionState.ACCEPTED);
        updateSuggestion(text, suggestionLog, suggestion);
    }

    private void updateSuggestion(String text, SuggestionLog suggestionLog, Suggestion suggestion) {
        Long channelId = suggestion.getChannel().getId();
        Long originalMessageId = suggestion.getMessageId();
        Long serverId = suggestion.getServer().getId();

        suggestionLog.setOriginalChannelId(channelId);
        suggestionLog.setOriginalMessageId(originalMessageId);
        suggestionLog.setOriginalMessageUrl(MessageUtils.buildMessageUrl(serverId, channelId, originalMessageId));
        AUserInAServer suggester = suggestion.getSuggester();
        JDA instance = botService.getInstance();
        Guild guildById = instance.getGuildById(serverId);
        if(guildById != null) {
            Member memberById = guildById.getMemberById(suggester.getUserReference().getId());
            if(memberById != null) {
                suggestionLog.setSuggester(memberById);
                suggestionLog.setSuggestion(suggestion);
                TextChannel textChannelById = guildById.getTextChannelById(channelId);
                if(textChannelById != null) {
                    textChannelById.retrieveMessageById(originalMessageId).queue(message ->
                        self.updateSuggestionMessageText(text, suggestionLog, message)
                    );
                }
            }
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
            postTargetService.sendEmbedInPostTarget(messageToSend, SUGGESTIONS_TARGET, suggestionLog.getServer().getId());
        }
    }

    @Override
    public void rejectSuggestion(Long suggestionId, String text, SuggestionLog log) {
        Suggestion suggestion = suggestionManagementService.getSuggestion(suggestionId).orElseThrow(() ->  new SuggestionNotFoundException(suggestionId));
        suggestionManagementService.setSuggestionState(suggestion, SuggestionState.REJECTED);
        updateSuggestion(text, log, suggestion);
    }

    @Override
    public void validateSetup(Long serverId) {
        postTargetService.throwIfPostTargetIsNotDefined(SUGGESTIONS_TARGET, serverId);
    }
}
