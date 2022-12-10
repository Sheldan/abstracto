package dev.sheldan.abstracto.moderation.listener.interaction;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.interaction.ComponentPayloadManagementService;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.modal.listener.ModalInteractionListener;
import dev.sheldan.abstracto.core.interaction.modal.listener.ModalInteractionListenerModel;
import dev.sheldan.abstracto.core.interaction.modal.listener.ModalInteractionListenerResult;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.service.MessageCache;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.model.interaction.MessageReportModalPayload;
import dev.sheldan.abstracto.moderation.service.ReactionReportService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static dev.sheldan.abstracto.moderation.service.ReactionReportServiceBean.REACTION_REPORT_FAILURE_RESPONSE_TEMPLATE;
import static dev.sheldan.abstracto.moderation.service.ReactionReportServiceBean.REACTION_REPORT_RESPONSE_TEMPLATE;

@Component
@Slf4j
public class ReportContextModalListener implements ModalInteractionListener {

    @Autowired
    private MessageCache messageCache;

    @Autowired
    private ReactionReportService reactionReportService;

    @Autowired
    private ComponentPayloadManagementService componentPayloadManagementService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public ModalInteractionListenerResult execute(ModalInteractionListenerModel model) {
        MessageReportModalPayload payload = (MessageReportModalPayload) model.getDeserializedPayload();
        String context = model
                .getEvent()
                .getValues()
                .stream()
                .filter(modalMapping -> modalMapping.getId().equals(payload.getTextInputId()))
                .map(ModalMapping::getAsString)
                .findFirst()
                .orElse(null);
        model.getEvent().deferReply(true).queue(interactionHook -> {
            messageCache.getMessageFromCache(payload.getServerId(), payload.getChannelId(), payload.getMessageId()).thenAccept(cachedMessage -> {
                ServerUser userReporting = ServerUser
                        .builder()
                        .serverId(model.getServerId())
                        .userId(cachedMessage.getAuthor().getAuthorId())
                        .isBot(cachedMessage.getAuthor().getIsBot())
                        .build();
                reactionReportService.createReactionReport(cachedMessage, userReporting, context)
                        .thenAccept(unused -> {
                            interactionService.sendMessageToInteraction(REACTION_REPORT_RESPONSE_TEMPLATE, new Object(), interactionHook);
                            log.info("Handled modal for message report with id {} in guild {} in channel {} on message {}",
                                    model.getEvent().getModalId(), payload.getServerId(), payload.getChannelId(), payload.getMessageId());
                            componentPayloadManagementService.deletePayload(payload.getModalId());
                        }).exceptionally(throwable -> {
                            interactionService.sendMessageToInteraction(REACTION_REPORT_FAILURE_RESPONSE_TEMPLATE, new Object(), interactionHook);
                            log.error("Failed to create reaction report in server {} on message {} in channel {} with interaction.",
                                    model.getServerId(), cachedMessage.getMessageId(), model.getEvent().getChannel().getIdLong(), throwable);
                            return null;
                        });
            }).exceptionally(throwable -> {
                interactionService.sendMessageToInteraction(REACTION_REPORT_FAILURE_RESPONSE_TEMPLATE, new Object(), interactionHook);
                log.error("Failed to load reported message for reporting message {} in channel {} with context.",
                        model.getEvent().getMessage().getIdLong(), model.getEvent().getChannel().getIdLong(), throwable);
                return null;
            });
        }, throwable -> {
            log.error("Failed to acknowledge modal interaction for report context modal listener in guild {} on message {}.", model.getServerId(),
                    model.getEvent().getMessage() != null ? model.getEvent().getMessage().getIdLong() : 0, throwable);
        });

        return ModalInteractionListenerResult.ACKNOWLEDGED;
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.REPORT_REACTIONS;
    }

    @Override
    public Integer getPriority() {
        return ListenerPriority.MEDIUM;
    }

    @Override
    public Boolean handlesEvent(ModalInteractionListenerModel model) {
        return model.getDeserializedPayload() instanceof MessageReportModalPayload && model.getEvent().isFromGuild();
    }
}
