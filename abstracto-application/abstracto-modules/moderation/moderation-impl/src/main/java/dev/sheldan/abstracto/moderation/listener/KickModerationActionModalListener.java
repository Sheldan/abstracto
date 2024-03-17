package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.interaction.InteractionExceptionService;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.modal.listener.ModalInteractionListener;
import dev.sheldan.abstracto.core.interaction.modal.listener.ModalInteractionListenerModel;
import dev.sheldan.abstracto.core.interaction.modal.listener.ModalInteractionListenerResult;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.model.interaction.ModerationActionKickPayload;
import dev.sheldan.abstracto.moderation.service.KickService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KickModerationActionModalListener implements ModalInteractionListener {

    private static final String KICK_MODERATION_ACTION_MODAL_RESPONSE_TEMPLATE = "moderationAction_kick_response";

    @Autowired
    private KickService kickService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private InteractionExceptionService interactionExceptionService;

    @Override
    public Boolean handlesEvent(ModalInteractionListenerModel model) {
        return KickModerationActionListener.KICK_MODAL_ORIGIN.equals(model.getOrigin());
    }

    @Override
    public ModalInteractionListenerResult execute(ModalInteractionListenerModel model) {
        ModerationActionKickPayload payload = (ModerationActionKickPayload) model.getDeserializedPayload();
        ServerUser userBeingKicked = ServerUser
                .builder()
                .userId(payload.getKickedUserId())
                .serverId(payload.getServerId())
                .build();

        ServerUser kickingUser = ServerUser.fromMember(model.getEvent().getMember());
        String reason = model
                .getEvent()
                .getValues()
                .stream()
                .filter(modalMapping -> modalMapping.getId().equals(payload.getReasonInputId()))
                .map(ModalMapping::getAsString)
                .findFirst()
                .orElse(null);
        log.debug("Handling kick moderation action modal interaction by user {} in server {}.", kickingUser.getUserId(), kickingUser.getServerId());
        model.getEvent().deferReply(true).queue(interactionHook -> {
            kickService.kickMember(model.getEvent().getGuild(), userBeingKicked, reason, kickingUser)
                    .thenCompose((future) -> FutureUtils.toSingleFutureGeneric(interactionService.sendMessageToInteraction(KICK_MODERATION_ACTION_MODAL_RESPONSE_TEMPLATE, new Object(), model.getEvent().getInteraction().getHook())))
                    .thenAccept(unused -> {
                log.info("Kicked user {} from server {}. Performed by user {}.", userBeingKicked.getUserId(), kickingUser.getServerId(), kickingUser.getUserId());
            }).exceptionally(throwable -> {
                interactionExceptionService.reportExceptionToInteraction(throwable, model, this);
                log.error("Failed to kick user {} from server {}. Performed by user {}.", userBeingKicked.getUserId(), kickingUser.getServerId(), kickingUser.getUserId(), throwable);
                return null;
            });
        });

        return ModalInteractionListenerResult.ACKNOWLEDGED;
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.MODERATION;
    }

    @Override
    public Integer getPriority() {
        return ListenerPriority.MEDIUM;
    }
}
