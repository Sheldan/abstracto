package dev.sheldan.abstracto.utility.service;

import dev.sheldan.abstracto.core.management.EmoteManagementService;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.database.PostTarget;
import dev.sheldan.abstracto.core.service.Bot;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.utils.MessageUtils;
import dev.sheldan.abstracto.templating.TemplateService;
import dev.sheldan.abstracto.utility.models.Suggestion;
import dev.sheldan.abstracto.utility.models.SuggestionState;
import dev.sheldan.abstracto.utility.models.template.SuggestionLog;
import dev.sheldan.abstracto.utility.service.management.AsyncSuggestionServiceBean;
import dev.sheldan.abstracto.utility.service.management.SuggestionManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SuggestionServiceBean implements SuggestionService {

    public static final String SUGGESTION_LOG_TEMPLATE = "suggest_log";
    public static final String SUGGESTION_YES_EMOTE = "SUGGESTION_YES";
    public static final String SUGGESTION_NO_EMOTE = "SUGGESTION_NO";
    @Autowired
    private SuggestionManagementService suggestionManagementService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private Bot botService;

    @Autowired
    private AsyncSuggestionServiceBean suggestionServiceBean;

    @Autowired
    private EmoteManagementService emoteManagementService;

    @Autowired
    private MessageService messageService;

    @Override
    public void createSuggestion(Member member, String text, SuggestionLog suggestionLog) {
        Suggestion suggestion = suggestionManagementService.createSuggestion(member, text);
        suggestionLog.setSuggestion(suggestion);
        suggestionLog.setText(text);
        MessageEmbed embed = templateService.renderEmbedTemplate(SUGGESTION_LOG_TEMPLATE, suggestionLog);
        long guildId = member.getGuild().getIdLong();
        JDA instance = botService.getInstance();
        Guild guildById = instance.getGuildById(guildId);
        if(guildById != null) {
            postTargetService.sendEmbedInPostTarget(embed, PostTarget.SUGGESTIONS, guildId).thenAccept(message -> {
                messageService.addReactionToMessage(SUGGESTION_YES_EMOTE, guildId, message);
                messageService.addReactionToMessage(SUGGESTION_NO_EMOTE, guildId, message);
                suggestionManagementService.setPostedMessage(suggestion, message);
            });
        } else {
            log.warn("Guild {} or member {} was not found when creating suggestion.", member.getGuild().getIdLong(), member.getIdLong());
        }
    }

    @Override
    public void acceptSuggestion(Long suggestionId, String text, SuggestionLog suggestionLog) {
        Suggestion suggestion = suggestionManagementService.getSuggestion(suggestionId);
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
                    textChannelById.retrieveMessageById(originalMessageId).queue(message -> {
                        suggestionServiceBean.updateSuggestionMessageText(text, suggestionLog, message);
                    });
                }
            }
        }
    }

    @Override
    public void rejectSuggestion(Long suggestionId, String text, SuggestionLog log) {
        Suggestion suggestion = suggestionManagementService.getSuggestion(suggestionId);
        suggestionManagementService.setSuggestionState(suggestion, SuggestionState.REJECTED);
        updateSuggestion(text, log, suggestion);
    }
}
