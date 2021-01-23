package dev.sheldan.abstracto.moderation.listener.async;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncMessageTextUpdatedListener;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.moderation.config.features.ModerationFeatures;
import dev.sheldan.abstracto.moderation.config.posttargets.LoggingPostTarget;
import dev.sheldan.abstracto.moderation.models.template.listener.MessageEditedLog;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MessageEditedListener implements AsyncMessageTextUpdatedListener {

    public static final String MESSAGE_EDITED_TEMPLATE = "message_edited";

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private BotService botService;

    @Override
    public void execute(CachedMessage messageBefore, CachedMessage messageAfter) {
        if(messageBefore.getContent().equals(messageAfter.getContent())) {
            log.trace("Message content was the same. Possible reason was: message was not in cache.");
            return;
        }
        botService.getMemberInServerAsync(messageAfter.getServerId(), messageAfter.getAuthor().getAuthorId()).thenAccept(author -> {
            log.trace("Message {} in channel {} in guild {} was edited.", messageBefore.getMessageId(), messageBefore.getChannelId(), messageBefore.getServerId());
            TextChannel textChannel = botService.getTextChannelFromServer(messageAfter.getServerId(), messageAfter.getChannelId());
            MessageEditedLog log = MessageEditedLog
                    .builder()
                    .messageAfter(messageAfter)
                    .messageBefore(messageBefore)
                    .messageChannel(textChannel)
                    .guild(textChannel.getGuild())
                    .member(author)
                    .build();
            MessageToSend message = templateService.renderEmbedTemplate(MESSAGE_EDITED_TEMPLATE, log);
            postTargetService.sendEmbedInPostTarget(message, LoggingPostTarget.EDIT_LOG, messageBefore.getServerId());
        }).exceptionally(throwable -> {
            log.error("Failed to load member {} for message edited listener in server {} for message {} in channel {}.",
                    messageAfter.getAuthor().getAuthorId(), messageAfter.getServerId(), messageAfter.getMessageId(), messageAfter.getChannelId(), throwable);
            return null;
        });
    }

    @Override
    public FeatureEnum getFeature() {
        return ModerationFeatures.LOGGING;
    }

}
