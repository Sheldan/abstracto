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
import dev.sheldan.abstracto.moderation.model.interaction.ModerationActionWarnPayload;
import dev.sheldan.abstracto.moderation.model.template.listener.ModerationActionPayloadModel;
import dev.sheldan.abstracto.moderation.model.template.listener.ModerationActionWarnModalModel;
import dev.sheldan.abstracto.moderation.service.ModerationActionServiceBean;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WarnModerationActionListener implements ButtonClickedListener {

    @Autowired
    private ModalService modalService;

    @Autowired
    private ComponentService componentService;

    @Autowired
    private ComponentPayloadManagementService componentPayloadManagementService;

    @Autowired
    private WarnModerationActionListener self;

    private static final String WARN_REASON_MODERATION_ACTION_MODAL = "moderationAction_warn";
    public static final String WARN_MODAL_ORIGIN = "WARN_MODERATION_ACTION_ORIGIN";

    @Override
    public ButtonClickedListenerResult execute(ButtonClickedListenerModel model) {
        ModerationActionPayloadModel payload = (ModerationActionPayloadModel) model.getDeserializedPayload();
        if(ModerationActionServiceBean.WARN_ACTION.equals(payload.getAction())) {
            log.info("Handling warn button interaction by user {} in server {} for user {}.",
                    payload.getUser().getUserId(), payload.getUser().getServerId(), model.getEvent().getMember().getIdLong());
            String modalId = componentService.generateComponentId();
            String reasonInputId = componentService.generateComponentId();
            ModerationActionWarnModalModel modalModel = ModerationActionWarnModalModel
                    .builder()
                    .modalId(modalId)
                    .reasonComponentId(reasonInputId)
                    .build();
            modalService.replyModal(model.getEvent(), WARN_REASON_MODERATION_ACTION_MODAL, modalModel).thenAccept(unused -> {
                log.info("Returned warn reason moderation action modal for user {} towards user {} in server {}.",
                        payload.getUser().getUserId(), model.getEvent().getMember().getIdLong(), model.getServerId());
                self.persistWarnModerationActionPayload(payload.getUser(), reasonInputId, modalId);
            }).exceptionally(throwable -> {
                log.error("Failed to show modal for warn moderation action.", throwable);
                return null;
            });
            return ButtonClickedListenerResult.ACKNOWLEDGED;
        } else {
            return ButtonClickedListenerResult.IGNORED;
        }
    }

    @Transactional
    public void persistWarnModerationActionPayload(ServerUser userToWarn, String reasonInput, String modalId) {
        ModerationActionWarnPayload payload = ModerationActionWarnPayload
                .builder()
                .warnedUserId(userToWarn.getUserId())
                .serverId(userToWarn.getServerId())
                .reasonInputId(reasonInput)
                .modalId(modalId)
                .build();
        ModalConfigPayload payloadConfig = ModalConfigPayload
                .builder()
                .modalPayload(payload)
                .origin(WARN_MODAL_ORIGIN)
                .payloadType(payload.getClass())
                .modalId(modalId)
                .build();
        componentPayloadManagementService.createModalPayload(payloadConfig, userToWarn.getServerId());
    }

    @Override
    public Boolean handlesEvent(ButtonClickedListenerModel model) {
        if(ModerationActionServiceBean.MODERATION_ACTION_ORIGIN.equals(model.getOrigin())){
            ModerationActionPayloadModel payload = (ModerationActionPayloadModel) model.getDeserializedPayload();
            return ModerationActionServiceBean.WARN_ACTION.equals(payload.getAction());
        }
        return false;
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.WARNING;
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
