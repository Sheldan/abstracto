package dev.sheldan.abstracto.logging.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncMessageDeletedListener;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.listener.MessageDeletedModel;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.logging.config.LoggingFeatureDefinition;
import dev.sheldan.abstracto.logging.config.LoggingPostTarget;
import dev.sheldan.abstracto.logging.model.template.MessageDeletedAttachmentLog;
import dev.sheldan.abstracto.logging.model.template.MessageDeletedLog;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class MessageDeleteLogListener implements AsyncMessageDeletedListener {

    public static final String MESSAGE_DELETED_TEMPLATE = "message_deleted";
    public static final String MESSAGE_DELETED_ATTACHMENT_TEMPLATE = "message_deleted_attachment";

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private MessageDeleteLogListener self;

    @Override
    public DefaultListenerResult execute(MessageDeletedModel model) {
        CachedMessage message = model.getCachedMessage();
        memberService.getMemberInServerAsync(model.getServerId(), message.getAuthor().getAuthorId()).thenAccept(member ->
            self.executeListener(message, member)
        ).exceptionally(throwable -> {
            log.warn("Could not retrieve member {} for message deleted event in guild {}.", message.getAuthor().getAuthorId(), model.getServerId(), throwable);
            return null;
        });
        return DefaultListenerResult.PROCESSED;
    }

    @Transactional
    public void executeListener(CachedMessage messageFromCache, Member authorMember) {
        log.debug("Message {} in channel {} in guild {} was deleted.", messageFromCache.getMessageId(), messageFromCache.getChannelId(), messageFromCache.getServerId());

        GuildMessageChannel textChannel = channelService.getMessageChannelFromServer(messageFromCache.getServerId(), messageFromCache.getChannelId());
        MessageDeletedLog logModel = MessageDeletedLog
                .builder()
                .cachedMessage(messageFromCache)
                .guild(authorMember.getGuild())
                .channel(textChannel)
                .member(authorMember)
                .build();
        MessageToSend message = templateService.renderEmbedTemplate(MESSAGE_DELETED_TEMPLATE, logModel, messageFromCache.getServerId());
        FutureUtils.toSingleFutureGeneric(postTargetService.sendEmbedInPostTarget(message, LoggingPostTarget.DELETE_LOG, messageFromCache.getServerId()))
        .exceptionally(throwable -> {
            log.error("Failed to send message deleted log.", throwable);
            return null;
        });
        if(messageFromCache.getAttachments() != null){
            log.debug("Notifying about deletions of {} attachments.", messageFromCache.getAttachments().size());
            for (int i = 0; i < messageFromCache.getAttachments().size(); i++) {
                MessageDeletedAttachmentLog attachmentLogModel = MessageDeletedAttachmentLog
                        .builder()
                        .imageUrl(messageFromCache.getAttachments().get(i).getProxyUrl())
                        .counter(i + 1)
                        .guild(authorMember.getGuild())
                        .channel(textChannel)
                        .member(authorMember)
                        .build();
                MessageToSend attachmentEmbed = templateService.renderEmbedTemplate(MESSAGE_DELETED_ATTACHMENT_TEMPLATE, attachmentLogModel, messageFromCache.getServerId());
                FutureUtils.toSingleFutureGeneric(postTargetService.sendEmbedInPostTarget(attachmentEmbed, LoggingPostTarget.DELETE_LOG, messageFromCache.getServerId()))
                .exceptionally(throwable -> {
                    log.error("Failed to send message deleted log.", throwable);
                    return null;
                });
            }
        }
    }

    @Override
    public FeatureDefinition getFeature() {
        return LoggingFeatureDefinition.LOGGING;
    }

}
