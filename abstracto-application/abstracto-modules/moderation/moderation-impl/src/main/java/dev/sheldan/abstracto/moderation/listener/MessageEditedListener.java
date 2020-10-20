package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.listener.MessageTextUpdatedListener;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.moderation.config.features.ModerationFeatures;
import dev.sheldan.abstracto.moderation.config.posttargets.LoggingPostTarget;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.moderation.models.template.listener.MessageEditedLog;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class MessageEditedListener implements MessageTextUpdatedListener {

    public static final String MESSAGE_EDITED_TEMPLATE = "message_edited";

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;

    @Override
    @Transactional
    public void execute(CachedMessage messageBefore, Message messageAfter) {
        if(messageBefore.getContent().equals(messageAfter.getContentRaw())){
            log.trace("Message content was the same. Possible reason was: message was not in cache.");
            return;
        }
        log.trace("Message {} in channel {} in guild {} was edited.", messageBefore.getMessageId(), messageBefore.getChannelId(), messageBefore.getServerId());
        MessageEditedLog log = MessageEditedLog
                .builder()
                .messageAfter(messageAfter)
                .messageBefore(messageBefore)
                .messageChannel(messageAfter.getTextChannel())
                .guild(messageAfter.getGuild())
                .member(messageAfter.getMember())
                .build();
        MessageToSend message = templateService.renderEmbedTemplate(MESSAGE_EDITED_TEMPLATE, log);
        postTargetService.sendEmbedInPostTarget(message, LoggingPostTarget.EDIT_LOG, messageBefore.getServerId());
    }

    @Override
    public FeatureEnum getFeature() {
        return ModerationFeatures.LOGGING;
    }

    @Override
    public Integer getPriority() {
        return ListenerPriority.MEDIUM;
    }
}
