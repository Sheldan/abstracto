package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.listener.MessageDeletedListener;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.service.MessageCache;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.utils.ContextUtils;
import dev.sheldan.abstracto.moderation.config.ModerationFeatures;
import dev.sheldan.abstracto.moderation.models.template.listener.MessageDeletedAttachmentLog;
import dev.sheldan.abstracto.moderation.models.template.listener.MessageDeletedLog;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MessageDeleteLogListener implements MessageDeletedListener {

    private static final String DELETE_LOG_TARGET = "deleteLog";
    private static String MESSAGE_DELETED_TEMPLATE = "message_deleted";
    private static String MESSAGE_DELETED_ATTACHMENT_TEMPLATE = "message_deleted_attachment";

    @Autowired
    private MessageCache messageCache;

    @Autowired
    private ContextUtils contextUtils;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;

    @Override
    public void execute(CachedMessage messageFromCache) {
        MessageDeletedLog logModel = (MessageDeletedLog) contextUtils.fromMessage(messageFromCache, MessageDeletedLog.class);
        logModel.setMessage(messageFromCache);
        String simpleMessageUpdatedMessage = templateService.renderTemplate(MESSAGE_DELETED_TEMPLATE, logModel);
        postTargetService.sendTextInPostTarget(simpleMessageUpdatedMessage, DELETE_LOG_TARGET, messageFromCache.getServerId());
        MessageToSend message = templateService.renderEmbedTemplate(MESSAGE_DELETED_TEMPLATE, logModel);
        postTargetService.sendEmbedInPostTarget(message, DELETE_LOG_TARGET, messageFromCache.getServerId());
        for (int i = 0; i < messageFromCache.getAttachmentUrls().size(); i++) {
            MessageDeletedAttachmentLog log = (MessageDeletedAttachmentLog) contextUtils.fromMessage(messageFromCache, MessageDeletedAttachmentLog.class);
            log.setImageUrl(messageFromCache.getAttachmentUrls().get(i));
            log.setCounter(i + 1);
            MessageToSend attachmentEmbed = templateService.renderEmbedTemplate(MESSAGE_DELETED_ATTACHMENT_TEMPLATE, log);
            postTargetService.sendEmbedInPostTarget(attachmentEmbed, DELETE_LOG_TARGET, messageFromCache.getServerId());
        }
    }

    @Override
    public String getFeature() {
        return ModerationFeatures.LOGGING;
    }
}
