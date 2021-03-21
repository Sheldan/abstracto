package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncMessageTextUpdatedListener;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.listener.MessageTextUpdatedModel;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.config.posttarget.LoggingPostTarget;
import dev.sheldan.abstracto.moderation.model.template.listener.MessageEditedLog;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
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
    private MemberService memberService;

    @Autowired
    private ChannelService channelService;

    @Override
    public DefaultListenerResult execute(MessageTextUpdatedModel model) {
        Message messageAfter = model.getAfter();
        CachedMessage messageBefore = model.getBefore();
        if(messageBefore.getContent().equals(messageAfter.getContentRaw())) {
            log.trace("Message content was the same. Possible reason was: message was not in cache.");
            return DefaultListenerResult.IGNORED;
        }
        log.trace("Message {} in channel {} in guild {} was edited.", messageBefore.getMessageId(), messageBefore.getChannelId(), messageBefore.getServerId());
        TextChannel textChannel = channelService.getTextChannelFromServer(model.getServerId(), messageBefore.getChannelId());
        MessageEditedLog log = MessageEditedLog
                .builder()
                .messageAfter(messageAfter)
                .messageBefore(messageBefore)
                .messageChannel(textChannel)
                .guild(textChannel.getGuild())
                .member(messageAfter.getMember())
                .build();
        MessageToSend message = templateService.renderEmbedTemplate(MESSAGE_EDITED_TEMPLATE, log, messageBefore.getServerId());
        postTargetService.sendEmbedInPostTarget(message, LoggingPostTarget.EDIT_LOG, messageBefore.getServerId());
        return DefaultListenerResult.PROCESSED;
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.LOGGING;
    }

}
