package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.interaction.ComponentPayloadManagementService;
import dev.sheldan.abstracto.core.interaction.ComponentService;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListener;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListenerModel;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListenerResult;
import dev.sheldan.abstracto.core.interaction.modal.ModalConfigPayload;
import dev.sheldan.abstracto.core.interaction.modal.ModalService;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.model.interaction.ModerationActionKickPayload;
import dev.sheldan.abstracto.moderation.model.template.listener.ModerationActionKickModalModel;
import dev.sheldan.abstracto.moderation.model.template.listener.ModerationActionPayloadModel;
import dev.sheldan.abstracto.moderation.service.ModerationActionServiceBean;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KickModerationActionListener implements ButtonClickedListener {

    @Autowired
    private ModalService modalService;

    @Autowired
    private ComponentService componentService;

    @Autowired
    private ComponentPayloadManagementService componentPayloadManagementService;

    @Autowired
    private KickModerationActionListener self;

    private static final String KICK_REASON_MODERATION_ACTION_MODAL = "moderationAction_kick";
    public static final String KICK_MODAL_ORIGIN = "KICK_MODERATION_ACTION_ORIGIN";

    @Override
    public ButtonClickedListenerResult execute(ButtonClickedListenerModel model) {
        ModerationActionPayloadModel payload = (ModerationActionPayloadModel) model.getDeserializedPayload();
        if(ModerationActionServiceBean.KICK_ACTION.equals(payload.getAction())) {
            log.info("Handling kick button interaction by user {} in server {} for user {}.",
                    payload.getUser().getUserId(), payload.getUser().getServerId(), model.getEvent().getMember().getIdLong());
            String modalId = componentService.generateComponentId();
            String reasonInputId = componentService.generateComponentId();
            ModerationActionKickModalModel modalModel = ModerationActionKickModalModel
                    .builder()
                    .modalId(modalId)
                    .reasonComponentId(reasonInputId)
                    .build();
            modalService.replyModal(model.getEvent(), KICK_REASON_MODERATION_ACTION_MODAL, modalModel).thenAccept(unused -> {
                log.info("Returned kick reason moderation action modal for user {} towards user {} in server {}.",
                        payload.getUser().getUserId(), model.getEvent().getMember().getIdLong(), model.getServerId());
                self.persistKickModerationActionPayload(payload.getUser(), reasonInputId, modalId);
            }).exceptionally(throwable -> {
                log.error("Failed to show modal for kick moderation action.", throwable);
                return null;
            });
            return ButtonClickedListenerResult.ACKNOWLEDGED;
        } else {
            return ButtonClickedListenerResult.IGNORED;
        }
    }

    @Transactional
    public void persistKickModerationActionPayload(ServerUser userToKick, String reasonInput, String modalId) {
        ModerationActionKickPayload payload = ModerationActionKickPayload
                .builder()
                .kickedUserId(userToKick.getUserId())
                .serverId(userToKick.getServerId())
                .reasonInputId(reasonInput)
                .modalId(modalId)
                .build();
        ModalConfigPayload payloadConfig = ModalConfigPayload
                .builder()
                .modalPayload(payload)
                .origin(KICK_MODAL_ORIGIN)
                .payloadType(payload.getClass())
                .modalId(modalId)
                .build();
        componentPayloadManagementService.createModalPayload(payloadConfig, userToKick.getServerId());
    }

    @Override
    public Boolean handlesEvent(ButtonClickedListenerModel model) {
        if(ModerationActionServiceBean.MODERATION_ACTION_ORIGIN.equals(model.getOrigin())){
            ModerationActionPayloadModel payload = (ModerationActionPayloadModel) model.getDeserializedPayload();
            return ModerationActionServiceBean.KICK_ACTION.equals(payload.getAction());
        }
        return false;
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.MODERATION;
    }

    @Override
    public Integer getPriority() {
        return ListenerPriority.MEDIUM;
    }

    @Override
    public Boolean autoAcknowledgeEvent() {
        return false;
    }
}
