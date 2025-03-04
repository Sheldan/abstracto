package dev.sheldan.abstracto.linkembed.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListenerResult;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListener;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListenerModel;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.core.interaction.ComponentPayloadManagementService;
import dev.sheldan.abstracto.linkembed.config.LinkEmbedFeatureDefinition;
import dev.sheldan.abstracto.linkembed.exception.LinkEmbedRemovalNotAllowedException;
import dev.sheldan.abstracto.linkembed.model.template.MessageEmbedDeleteButtonPayload;
import dev.sheldan.abstracto.linkembed.service.MessageEmbedMetricService;
import dev.sheldan.abstracto.linkembed.service.MessageEmbedServiceBean;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class MessageEmbedDeleteButtonClickedListener implements ButtonClickedListener {

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageEmbedMetricService messageEmbedMetricService;

    @Autowired
    private ComponentPayloadManagementService componentPayloadManagementService;

    @Autowired
    private MessageEmbedDeleteButtonClickedListener self;

    @Override
    public ButtonClickedListenerResult execute(ButtonClickedListenerModel model) {
        ButtonInteractionEvent event = model.getEvent();
        MessageEmbedDeleteButtonPayload payload = (MessageEmbedDeleteButtonPayload) model.getDeserializedPayload();
        Long clickingUserId = event.getInteraction().getUser().getIdLong();
        boolean embeddedUserRemoves = clickingUserId.equals(payload.getEmbeddedUserId());
        if(embeddedUserRemoves || clickingUserId.equals(payload.getEmbeddingUserId())) {
            messageService.deleteMessageInChannelInServer(payload.getEmbeddingServerId(), payload.getEmbeddingChannelId(), payload.getEmbeddingMessageId())
                    .thenAccept(aVoid -> self.executeAfterDeletion(payload, clickingUserId, embeddedUserRemoves, event.getComponentId()));
        } else {
            log.info("Not the original author or embedding user clicked the button of component {} in server {} in channel {} on message {}.",
                    event.getComponentId(), event.getGuild().getIdLong(), event.getGuildChannel().getIdLong(), event.getMessageId());
            throw new LinkEmbedRemovalNotAllowedException();
        }
        return ButtonClickedListenerResult.ACKNOWLEDGED;
    }

    @Transactional
    public void executeAfterDeletion(MessageEmbedDeleteButtonPayload payload, Long clickingUserId, boolean embeddedUserRemoves, String componentId) {
        log.info("User {} deleted embedding message {} in channel {} in server {} from embedded message {} in channel {} and server {}.",
                clickingUserId, payload.getEmbeddingMessageId(), payload.getEmbeddingChannelId(), payload.getEmbeddingServerId(),
                payload.getEmbeddedMessageId(), payload.getEmbeddedChannelId(), payload.getEmbeddedServerId());
        messageEmbedMetricService.incrementMessageEmbedDeletedMetric(embeddedUserRemoves);
        componentPayloadManagementService.deletePayload(componentId);
    }

    @Override
    public Boolean handlesEvent(ButtonClickedListenerModel model) {
        return MessageEmbedServiceBean.MESSAGE_EMBED_DELETE_ORIGIN.equals(model.getOrigin()) && model.getEvent().isFromGuild();
    }

    @Override
    public FeatureDefinition getFeature() {
        return LinkEmbedFeatureDefinition.LINK_EMBEDS;
    }


    @Override
    public Integer getPriority() {
        return 10;
    }
}
