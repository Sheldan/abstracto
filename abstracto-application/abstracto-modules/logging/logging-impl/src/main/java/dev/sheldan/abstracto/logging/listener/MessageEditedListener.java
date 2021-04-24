package dev.sheldan.abstracto.logging.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncMessageUpdatedListener;
import dev.sheldan.abstracto.core.models.cache.CachedAttachment;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.listener.MessageUpdatedModel;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.logging.config.LoggingFeatureDefinition;
import dev.sheldan.abstracto.logging.config.LoggingPostTarget;
import dev.sheldan.abstracto.logging.model.template.MessageDeletedAttachmentLog;
import dev.sheldan.abstracto.logging.model.template.MessageEditedLog;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MessageEditedListener implements AsyncMessageUpdatedListener {

    public static final String MESSAGE_EDITED_TEMPLATE = "message_edited";
    public static final String MESSAGE_EDITED_ATTACHMENT_REMOVED_TEMPLATE = "message_edited_attachment_removed";

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private ChannelService channelService;

    @Override
    public DefaultListenerResult execute(MessageUpdatedModel model) {
        Message messageAfter = model.getAfter();
        CachedMessage messageBefore = model.getBefore();
        int attachmentCountBefore = messageBefore.getAttachments() != null ? messageBefore.getAttachments().size() : 0;
        int attachmentCountAfter = messageAfter.getAttachments().size();
        boolean attachmentWasRemoved = attachmentCountAfter != attachmentCountBefore;
        if(messageBefore.getContent().equals(messageAfter.getContentRaw()) && attachmentCountBefore == attachmentCountAfter) {
            log.info("Message content was the same and attachment count did not change. Possible reason was: message was not in cache.");
            return DefaultListenerResult.IGNORED;
        }
        log.debug("Message {} in channel {} in guild {} was edited.", messageBefore.getMessageId(), messageBefore.getChannelId(), model.getServerId());
        TextChannel textChannel = channelService.getTextChannelFromServer(model.getServerId(), messageBefore.getChannelId());
        MessageEditedLog lodModel = MessageEditedLog
                .builder()
                .messageAfter(messageAfter)
                .messageBefore(messageBefore)
                .messageChannel(textChannel)
                .guild(textChannel.getGuild())
                .member(messageAfter.getMember())
                .build();
        MessageToSend message = templateService.renderEmbedTemplate(MESSAGE_EDITED_TEMPLATE, lodModel, model.getServerId());
        postTargetService.sendEmbedInPostTarget(message, LoggingPostTarget.EDIT_LOG, model.getServerId());

        if(attachmentWasRemoved) {
            log.info("Attachment count changed. Old {}, new {}.", attachmentCountBefore, attachmentCountAfter);
            // this filters attachments which were present in the cached message, but arent anymore in the new one
            List<CachedAttachment> removedAttachments = messageBefore.getAttachments().stream().filter(cachedAttachment ->
                messageAfter.getAttachments().stream().noneMatch(attachment -> attachment.getIdLong() == cachedAttachment.getId())
            ).collect(Collectors.toList());
            log.debug("Logging deletion of {} attachments.", removedAttachments.size());
            for (int i = 0; i < removedAttachments.size(); i++) {
                MessageDeletedAttachmentLog log = MessageDeletedAttachmentLog
                        .builder()
                        .imageUrl(removedAttachments.get(i).getProxyUrl())
                        .counter(i + 1)
                        .guild(messageAfter.getGuild())
                        .channel(textChannel)
                        .member(messageAfter.getMember())
                        .build();
                MessageToSend attachmentEmbed = templateService.renderEmbedTemplate(MESSAGE_EDITED_ATTACHMENT_REMOVED_TEMPLATE,
                        log, messageBefore.getServerId());
                postTargetService.sendEmbedInPostTarget(attachmentEmbed, LoggingPostTarget.DELETE_LOG, messageBefore.getServerId());
            }
        }
        return DefaultListenerResult.PROCESSED;
    }

    @Override
    public FeatureDefinition getFeature() {
        return LoggingFeatureDefinition.LOGGING;
    }

}
