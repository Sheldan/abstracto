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
import dev.sheldan.abstracto.moderation.model.interaction.ModerationActionMutePayload;
import dev.sheldan.abstracto.moderation.model.template.listener.ModerationActionMuteModalModel;
import dev.sheldan.abstracto.moderation.model.template.listener.ModerationActionPayloadModel;
import dev.sheldan.abstracto.moderation.service.ModerationActionServiceBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class MuteModerationActionListener implements ButtonClickedListener {

    @Autowired
    private ComponentService componentService;

    @Autowired
    private ModalService modalService;

    @Autowired
    private ComponentPayloadManagementService componentPayloadManagementService;

    @Autowired
    private MuteModerationActionListener self;

    private static final String MUTE_REASON_MODERATION_ACTION_MODAL = "moderationAction_mute";
    public static final String MUTE_MODAL_ORIGIN = "MUTE_MODERATION_ACTION_ORIGIN";

    @Override
    public ButtonClickedListenerResult execute(ButtonClickedListenerModel model) {
        ModerationActionPayloadModel payload = (ModerationActionPayloadModel) model.getDeserializedPayload();
        if(ModerationActionServiceBean.MUTE_ACTION.equals(payload.getAction())) {
            log.info("Handling mute button interaction by user {} in server {} for user {}.",
                    payload.getUser().getUserId(), payload.getUser().getServerId(), model.getEvent().getMember().getIdLong());
            String modalId = componentService.generateComponentId();
            String reasonInputId = componentService.generateComponentId();
            String durationInputId = componentService.generateComponentId();
            ModerationActionMuteModalModel modalModel = ModerationActionMuteModalModel
                    .builder()
                    .modalId(modalId)
                    .durationComponentId(durationInputId)
                    .reasonComponentId(reasonInputId)
                    .build();
            modalService.replyModal(model.getEvent(), MUTE_REASON_MODERATION_ACTION_MODAL, modalModel).thenAccept(unused -> {
                log.info("Returned mute reason moderation action modal for user {} towards user {} in server {}.",
                        payload.getUser().getUserId(), model.getEvent().getMember().getIdLong(), model.getServerId());
                self.persistMuteModerationActionPayload(payload.getUser(), reasonInputId, modalId, durationInputId);
            }).exceptionally(throwable -> {
                log.error("Failed to show modal for mute moderation action.", throwable);
                return null;
            });
            return ButtonClickedListenerResult.ACKNOWLEDGED;
        } else {
            return ButtonClickedListenerResult.IGNORED;
        }
    }

    @Transactional
    public void persistMuteModerationActionPayload(ServerUser userToMute, String reasonInput, String modalId, String durationInputId) {
        ModerationActionMutePayload payload = ModerationActionMutePayload
                .builder()
                .mutedUserId(userToMute.getUserId())
                .serverId(userToMute.getServerId())
                .reasonInputId(reasonInput)
                .durationInputId(durationInputId)
                .modalId(modalId)
                .build();
        ModalConfigPayload payloadConfig = ModalConfigPayload
                .builder()
                .modalPayload(payload)
                .origin(MUTE_MODAL_ORIGIN)
                .payloadType(payload.getClass())
                .modalId(modalId)
                .build();
        componentPayloadManagementService.createModalPayload(payloadConfig, userToMute.getServerId());
    }

    @Override
    public Boolean handlesEvent(ButtonClickedListenerModel model) {
        if(ModerationActionServiceBean.MODERATION_ACTION_ORIGIN.equals(model.getOrigin())){
            ModerationActionPayloadModel payload = (ModerationActionPayloadModel) model.getDeserializedPayload();
            return ModerationActionServiceBean.MUTE_ACTION.equals(payload.getAction());
        }
        return false;
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.MUTING;
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
