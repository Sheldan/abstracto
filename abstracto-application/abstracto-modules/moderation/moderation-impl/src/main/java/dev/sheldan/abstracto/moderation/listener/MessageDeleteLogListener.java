package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncMessageDeletedListener;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.ContextUtils;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.config.posttarget.LoggingPostTarget;
import dev.sheldan.abstracto.moderation.model.template.listener.MessageDeletedAttachmentLog;
import dev.sheldan.abstracto.moderation.model.template.listener.MessageDeletedLog;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class MessageDeleteLogListener implements AsyncMessageDeletedListener {

    public static final String MESSAGE_DELETED_TEMPLATE = "message_deleted";
    public static final String MESSAGE_DELETED_ATTACHMENT_TEMPLATE = "message_deleted_attachment";

    @Autowired
    private ContextUtils contextUtils;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private MessageDeleteLogListener self;

    @Override
    public void execute(CachedMessage messageFromCache) {
        memberService.getMemberInServerAsync(messageFromCache.getServerId(), messageFromCache.getAuthor().getAuthorId()).thenAccept(member ->
            self.executeListener(messageFromCache, member)
        );
    }

    @Transactional
    public void executeListener(CachedMessage messageFromCache, Member authorMember) {
        log.trace("Message {} in channel {} in guild {} was deleted.", messageFromCache.getMessageId(), messageFromCache.getChannelId(), messageFromCache.getServerId());

        TextChannel textChannel = channelService.getTextChannelFromServer(messageFromCache.getServerId(), messageFromCache.getChannelId());
        MessageDeletedLog logModel = MessageDeletedLog
                .builder()
                .cachedMessage(messageFromCache)
                .guild(authorMember.getGuild())
                .channel(textChannel)
                .member(authorMember)
                .build();
        MessageToSend message = templateService.renderEmbedTemplate(MESSAGE_DELETED_TEMPLATE, logModel, messageFromCache.getServerId());
        postTargetService.sendEmbedInPostTarget(message, LoggingPostTarget.DELETE_LOG, messageFromCache.getServerId());
        if(messageFromCache.getAttachments() != null){
            log.trace("Notifying about deletions of {} attachments.", messageFromCache.getAttachments().size());
            for (int i = 0; i < messageFromCache.getAttachments().size(); i++) {
                MessageDeletedAttachmentLog log = MessageDeletedAttachmentLog
                        .builder()
                        .imageUrl(messageFromCache.getAttachments().get(i).getProxyUrl())
                        .counter(i + 1)
                        .guild(authorMember.getGuild())
                        .channel(textChannel)
                        .member(authorMember)
                        .build();
                MessageToSend attachmentEmbed = templateService.renderEmbedTemplate(MESSAGE_DELETED_ATTACHMENT_TEMPLATE, log, messageFromCache.getServerId());
                postTargetService.sendEmbedInPostTarget(attachmentEmbed, LoggingPostTarget.DELETE_LOG, messageFromCache.getServerId());
            }
        }
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.LOGGING;
    }

}
