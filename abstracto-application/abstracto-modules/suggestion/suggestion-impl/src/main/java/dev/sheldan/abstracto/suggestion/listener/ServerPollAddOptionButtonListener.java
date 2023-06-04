package dev.sheldan.abstracto.suggestion.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.interaction.ComponentPayloadManagementService;
import dev.sheldan.abstracto.core.interaction.ComponentService;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListener;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListenerModel;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListenerResult;
import dev.sheldan.abstracto.core.interaction.modal.ModalConfigPayload;
import dev.sheldan.abstracto.core.interaction.modal.ModalService;
import dev.sheldan.abstracto.suggestion.config.SuggestionFeatureDefinition;
import dev.sheldan.abstracto.suggestion.model.payload.PollAddOptionButtonPayload;
import dev.sheldan.abstracto.suggestion.model.template.PollAddOptionModalModel;
import dev.sheldan.abstracto.suggestion.model.payload.PollAddOptionModalPayload;
import dev.sheldan.abstracto.suggestion.service.PollServiceBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class ServerPollAddOptionButtonListener implements ButtonClickedListener {

    @Autowired
    private ComponentService componentService;

    @Autowired
    private ModalService modalService;

    @Autowired
    private ServerPollAddOptionButtonListener self;

    @Autowired
    private ComponentPayloadManagementService componentPayloadManagementService;

    private static final String SERVER_POLL_ADD_OPTION_MODAL_TEMPLATE = "poll_add_option";
    public static final String SERVER_POLL_ADD_OPTION_MODAL_ORIGIN = "SERVER_POLL_ADD_OPTION_MODAL";

    @Override
    public ButtonClickedListenerResult execute(ButtonClickedListenerModel model) {
        PollAddOptionButtonPayload payload = (PollAddOptionButtonPayload) model.getDeserializedPayload();
        String modalId = componentService.generateComponentId();
        String labelInputId = componentService.generateComponentId();
        String descriptionInputId = componentService.generateComponentId();
        PollAddOptionModalModel modalModel = PollAddOptionModalModel
                .builder()
                .descriptionInputComponentId(descriptionInputId)
                .modalId(modalId)
                .labelInputComponentId(labelInputId)
                .build();
        modalService.replyModal(model.getEvent(), SERVER_POLL_ADD_OPTION_MODAL_TEMPLATE, modalModel).thenAccept(unused -> {
            log.info("Opened a model for entering a new option for poll {} in server {} for user {}.",
                    payload.getPollId(), payload.getServerId(), model.getEvent().getMember().getIdLong());
            self.persistModalPayload(modalModel, model.getServerId(), payload.getPollId());
        }).exceptionally(throwable -> {
            log.error("Failed to show modal for entering a new option for poll {} in server {} for user {}.",
                    payload.getPollId(), payload.getServerId(), model.getEvent().getMember().getIdLong(), throwable);
            return null;
        });
        return ButtonClickedListenerResult.ACKNOWLEDGED;
    }

    @Transactional
    public void persistModalPayload(PollAddOptionModalModel model, Long serverId, Long pollId) {
        PollAddOptionModalPayload payload = PollAddOptionModalPayload
                .builder()
                .modalId(model.getModalId())
                .labelInputComponentId(model.getLabelInputComponentId())
                .descriptionInputComponentId(model.getDescriptionInputComponentId())
                .serverId(serverId)
                .pollId(pollId)
                .build();
        ModalConfigPayload payloadConfig = ModalConfigPayload
                .builder()
                .modalPayload(payload)
                .origin(SERVER_POLL_ADD_OPTION_MODAL_ORIGIN)
                .payloadType(payload.getClass())
                .modalId(model.getModalId())
                .build();
        componentPayloadManagementService.createModalPayload(payloadConfig, serverId);
    }

    @Override
    public Boolean autoAcknowledgeEvent() {
        return false;
    }

    @Override
    public Boolean handlesEvent(ButtonClickedListenerModel model) {
        return PollServiceBean.SERVER_POLL_ADD_OPTION_ORIGIN.equals(model.getOrigin());
    }

    @Override
    public FeatureDefinition getFeature() {
        return SuggestionFeatureDefinition.POLL;
    }

    @Override
    public Integer getPriority() {
        return ListenerPriority.MEDIUM;
    }
}
