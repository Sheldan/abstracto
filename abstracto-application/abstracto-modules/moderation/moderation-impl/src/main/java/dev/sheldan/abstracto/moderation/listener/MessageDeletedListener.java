package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.ContextUtils;
import dev.sheldan.abstracto.core.models.database.PostTarget;
import dev.sheldan.abstracto.core.service.MessageCache;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.moderation.models.template.listener.MessageDeletedLog;
import dev.sheldan.abstracto.templating.TemplateService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;

@Component
public class MessageDeletedListener extends ListenerAdapter {

    private static String MESSAGE_DELETED_TEMPLATE = "message_deleted";

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
        postTargetService.sendTextInPostTarget(simpleMessageUpdatedMessage, PostTarget.EDIT_LOG, event.getGuild().getIdLong());
        messageFromCache.getAttachments().forEach(attachment -> {

        });
    }
}
