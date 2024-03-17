package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.interaction.InteractionExceptionService;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.modal.listener.ModalInteractionListener;
import dev.sheldan.abstracto.core.interaction.modal.listener.ModalInteractionListenerModel;
import dev.sheldan.abstracto.core.interaction.modal.listener.ModalInteractionListenerResult;
import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.model.interaction.ModerationActionWarnPayload;
import dev.sheldan.abstracto.moderation.service.WarnService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static dev.sheldan.abstracto.moderation.command.Warn.WARN_DEFAULT_REASON_TEMPLATE;

@Component
@Slf4j
public class WarnModerationActionModalListener implements ModalInteractionListener {

    private static final String WARN_MODERATION_ACTION_MODAL_RESPONSE_TEMPLATE = "moderationAction_warn_response";

    @Autowired
    private WarnService warnService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private InteractionExceptionService interactionExceptionService;

    @Override
    public Boolean handlesEvent(ModalInteractionListenerModel model) {
        return WarnModerationActionListener.WARN_MODAL_ORIGIN.equals(model.getOrigin());
    }

    @Override
    public ModalInteractionListenerResult execute(ModalInteractionListenerModel model) {
        ModerationActionWarnPayload payload = (ModerationActionWarnPayload) model.getDeserializedPayload();
        ServerUser userBeingWarned = ServerUser
                .builder()
                .userId(payload.getWarnedUserId())
                .serverId(payload.getServerId())
                .build();

        ServerUser warningUser = ServerUser.fromMember(model.getEvent().getMember());
        String reason;
        String tempReason = model
                .getEvent()
                .getValues()
                .stream()
                .filter(modalMapping -> modalMapping.getId().equals(payload.getReasonInputId()))
                .map(ModalMapping::getAsString)
                .findFirst()
                .orElse(null);
        if(StringUtils.isBlank(tempReason)) {
            reason = templateService.renderSimpleTemplate(WARN_DEFAULT_REASON_TEMPLATE, model.getServerId());
        } else {
            reason = tempReason;
        }
        ServerChannelMessage serverChannelMessage = ServerChannelMessage.fromMessage(model.getEvent().getMessage());
        log.debug("Handling warn moderation action modal interaction by user {} in server {}.", warningUser.getUserId(), warningUser.getServerId());
        model.getEvent().deferReply(true).queue(interactionHook -> {
            warnService.warnUserWithLog(model.getEvent().getGuild(), userBeingWarned, warningUser, reason, serverChannelMessage)
                    .thenCompose((future) -> FutureUtils.toSingleFutureGeneric(interactionService.sendMessageToInteraction(WARN_MODERATION_ACTION_MODAL_RESPONSE_TEMPLATE, new Object(), model.getEvent().getInteraction().getHook())))
                    .thenAccept(unused -> {
                log.info("Warned user {} in server {}. Performed by user {}.", userBeingWarned.getUserId(), warningUser.getServerId(), warningUser.getUserId());
            }).exceptionally(throwable -> {
                interactionExceptionService.reportExceptionToInteraction(throwable, model, this);
                log.error("Failed to warn user {} from server {}. Performed by user {}.", userBeingWarned.getUserId(), warningUser.getServerId(), warningUser.getUserId(), throwable);
                return null;
            });
        });

        return ModalInteractionListenerResult.ACKNOWLEDGED;
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.WARNING;
    }

    @Override
    public Integer getPriority() {
        return ListenerPriority.MEDIUM;
    }
}
