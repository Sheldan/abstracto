package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.models.CachedMessage;
import dev.sheldan.abstracto.core.utils.ContextUtils;
import dev.sheldan.abstracto.core.service.MessageCache;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.moderation.models.template.listener.MessageDeletedAttachmentLog;
import dev.sheldan.abstracto.moderation.models.template.listener.MessageDeletedLog;
import dev.sheldan.abstracto.core.models.embed.MessageToSend;
import dev.sheldan.abstracto.templating.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
public class MessageDeletedListener extends ListenerAdapter {

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

    @Autowired
    private MessageDeletedListener self;

    @Override
    @Transactional
    public void onGuildMessageDelete(@Nonnull GuildMessageDeleteEvent event) {
        messageCache.getMessageFromCache(event.getGuild().getIdLong(), event.getChannel().getIdLong(), event.getMessageIdLong())
        .thenAccept(messageFromCache -> {
            try {
                self.logMessage(event, messageFromCache);
            } catch (Exception e) {
                log.error("Error when logging message deletion.", e);
            }
        });

    }

    @Transactional
    public void logMessage(@Nonnull GuildMessageDeleteEvent event, CachedMessage messageFromCache) {
        MessageDeletedLog logModel = (MessageDeletedLog) contextUtils.fromMessage(messageFromCache, MessageDeletedLog.class);
        logModel.setMessage(messageFromCache);
        String simpleMessageUpdatedMessage = templateService.renderTemplate(MESSAGE_DELETED_TEMPLATE, logModel);
        postTargetService.sendTextInPostTarget(simpleMessageUpdatedMessage, DELETE_LOG_TARGET, event.getGuild().getIdLong());
        MessageToSend message = templateService.renderEmbedTemplate(MESSAGE_DELETED_TEMPLATE, logModel);
        postTargetService.sendEmbedInPostTarget(message, DELETE_LOG_TARGET, event.getGuild().getIdLong());
        for (int i = 0; i < messageFromCache.getAttachmentUrls().size(); i++) {
            MessageDeletedAttachmentLog log = (MessageDeletedAttachmentLog) contextUtils.fromMessage(messageFromCache, MessageDeletedAttachmentLog.class);
            log.setImageUrl(messageFromCache.getAttachmentUrls().get(i));
            log.setCounter(i + 1);
            MessageToSend attachmentEmbed = templateService.renderEmbedTemplate(MESSAGE_DELETED_ATTACHMENT_TEMPLATE, log);
            postTargetService.sendEmbedInPostTarget(attachmentEmbed, DELETE_LOG_TARGET, event.getGuild().getIdLong());
        }
    }
}
