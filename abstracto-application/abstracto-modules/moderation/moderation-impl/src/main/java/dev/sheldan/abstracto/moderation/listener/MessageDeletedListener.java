package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.utils.ContextUtils;
import dev.sheldan.abstracto.core.service.MessageCache;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.moderation.models.template.listener.MessageDeletedAttachmentLog;
import dev.sheldan.abstracto.moderation.models.template.listener.MessageDeletedLog;
import dev.sheldan.abstracto.templating.TemplateService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;

@Component
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

    @Override
    @Transactional
    public void onGuildMessageDelete(@Nonnull GuildMessageDeleteEvent event) {
        Message messageFromCache = messageCache.getMessageFromCache(event.getMessageIdLong(),
                event.getChannel().getIdLong(), event.getGuild().getIdLong());
        MessageDeletedLog logModel = (MessageDeletedLog) contextUtils.fromMessage(messageFromCache, MessageDeletedLog.class);
        logModel.setMessage(messageFromCache);
        String simpleMessageUpdatedMessage = templateService.renderTemplate(MESSAGE_DELETED_TEMPLATE, logModel);
        postTargetService.sendTextInPostTarget(simpleMessageUpdatedMessage, DELETE_LOG_TARGET, event.getGuild().getIdLong());
        MessageEmbed embed = templateService.renderEmbedTemplate(MESSAGE_DELETED_TEMPLATE, logModel);
        postTargetService.sendEmbedInPostTarget(embed, DELETE_LOG_TARGET, event.getGuild().getIdLong());
        for (int i = 0; i < messageFromCache.getAttachments().size(); i++) {
            Message.Attachment attachment = messageFromCache.getAttachments().get(i);
            MessageDeletedAttachmentLog log = (MessageDeletedAttachmentLog) contextUtils.fromMessage(messageFromCache, MessageDeletedAttachmentLog.class);
            log.setImageUrl(attachment.getProxyUrl());
            log.setCounter(i + 1);
            MessageEmbed attachmentEmbed = templateService.renderEmbedTemplate(MESSAGE_DELETED_ATTACHMENT_TEMPLATE, log);
            postTargetService.sendEmbedInPostTarget(attachmentEmbed, DELETE_LOG_TARGET, event.getGuild().getIdLong());
        }
    }
}
