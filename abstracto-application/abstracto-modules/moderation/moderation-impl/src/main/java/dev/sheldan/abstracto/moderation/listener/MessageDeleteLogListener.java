package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.listener.MessageDeletedListener;
import dev.sheldan.abstracto.core.models.AServerAChannelAUser;
import dev.sheldan.abstracto.core.models.GuildChannelMember;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.moderation.config.features.ModerationFeatures;
import dev.sheldan.abstracto.moderation.config.posttargets.LoggingPostTarget;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.utils.ContextUtils;
import dev.sheldan.abstracto.moderation.models.template.listener.MessageDeletedAttachmentLog;
import dev.sheldan.abstracto.moderation.models.template.listener.MessageDeletedLog;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MessageDeleteLogListener implements MessageDeletedListener {

    public static final String MESSAGE_DELETED_TEMPLATE = "message_deleted";
    public static final String MESSAGE_DELETED_ATTACHMENT_TEMPLATE = "message_deleted_attachment";

    @Autowired
    private ContextUtils contextUtils;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;

    @Override
    public void execute(CachedMessage messageFromCache, AServerAChannelAUser authorUser, GuildChannelMember authorMember) {
        log.trace("Message {} in channel {} in guild {} was deleted.", messageFromCache.getMessageId(), messageFromCache.getChannelId(), messageFromCache.getServerId());
        MessageDeletedLog logModel = MessageDeletedLog
                .builder()
                .cachedMessage(messageFromCache)
                .server(authorUser.getGuild())
                .channel(authorUser.getChannel())
                .user(authorUser.getUser())
                .aUserInAServer(authorUser.getAUserInAServer())
                .guild(authorMember.getGuild())
                .messageChannel(authorMember.getTextChannel())
                .member(authorMember.getMember())
                .build();
        MessageToSend message = templateService.renderEmbedTemplate(MESSAGE_DELETED_TEMPLATE, logModel);
        postTargetService.sendEmbedInPostTarget(message, LoggingPostTarget.DELETE_LOG, messageFromCache.getServerId());
        if(messageFromCache.getAttachmentUrls() != null){
            for (int i = 0; i < messageFromCache.getAttachmentUrls().size(); i++) {
                MessageDeletedAttachmentLog log = MessageDeletedAttachmentLog
                        .builder()
                        .imageUrl(messageFromCache.getAttachmentUrls().get(i))
                        .counter(i + 1)
                        .server(authorUser.getGuild())
                        .channel(authorUser.getChannel())
                        .user(authorUser.getUser())
                        .aUserInAServer(authorUser.getAUserInAServer())
                        .guild(authorMember.getGuild())
                        .messageChannel(authorMember.getTextChannel())
                        .member(authorMember.getMember())
                        .build();
                MessageToSend attachmentEmbed = templateService.renderEmbedTemplate(MESSAGE_DELETED_ATTACHMENT_TEMPLATE, log);
                postTargetService.sendEmbedInPostTarget(attachmentEmbed, LoggingPostTarget.DELETE_LOG, messageFromCache.getServerId());
            }
        }
    }

    @Override
    public FeatureEnum getFeature() {
        return ModerationFeatures.LOGGING;
    }
}
