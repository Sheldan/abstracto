package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.interaction.InteractionExceptionService;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.modal.listener.ModalInteractionListener;
import dev.sheldan.abstracto.core.interaction.modal.listener.ModalInteractionListenerModel;
import dev.sheldan.abstracto.core.interaction.modal.listener.ModalInteractionListenerResult;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.core.utils.ParseUtils;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.model.interaction.ModerationActionBanPayload;
import dev.sheldan.abstracto.moderation.service.BanService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Slf4j
public class BanModerationActionModalListener implements ModalInteractionListener {

    private static final String KICK_MODERATION_ACTION_MODAL_RESPONSE_TEMPLATE = "moderationAction_ban_response";
    private static final String DEFAULT_BAN_REASON_TEMPLATE_KEY = "ban_default_reason";

    @Autowired
    private BanService banService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private InteractionExceptionService interactionExceptionService;

    @Autowired
    private TemplateService templateService;

    @Override
    public Boolean handlesEvent(ModalInteractionListenerModel model) {
        return BanModerationActionListener.BAN_MODAL_ORIGIN.equals(model.getOrigin());
    }

    @Override
    public ModalInteractionListenerResult execute(ModalInteractionListenerModel model) {
        ModerationActionBanPayload payload = (ModerationActionBanPayload) model.getDeserializedPayload();
        ServerUser userBeingBanned = ServerUser
                .builder()
                .userId(payload.getBannedUserId())
                .serverId(payload.getServerId())
                .build();

        ServerUser kickingUser = ServerUser.fromMember(model.getEvent().getMember());

        String duration = model
                .getEvent()
                .getValues()
                .stream()
                .filter(modalMapping -> modalMapping.getId().equals(payload.getDurationInputId()))
                .map(ModalMapping::getAsString)
                .findFirst()
                .orElse(null);
        Duration messageDeletionDuration;
        if(duration != null) {
            messageDeletionDuration = ParseUtils.parseDuration(duration.trim());
        } else {
            messageDeletionDuration = null;
        }
        String reason;
        String tempReason = model
                .getEvent()
                .getValues()
                .stream()
                .filter(modalMapping -> modalMapping.getId().equals(payload.getDurationInputId()))
                .map(ModalMapping::getAsString)
                .findFirst()
                .orElse(null);
        if(StringUtils.isBlank(tempReason)) {
            reason = templateService.renderSimpleTemplate(DEFAULT_BAN_REASON_TEMPLATE_KEY);
        } else {
            reason = tempReason;
        }
        log.debug("Handling ban moderation action modal interaction by user {} in server {}.", kickingUser.getUserId(), kickingUser.getServerId());
        model.getEvent().deferReply(true).queue(interactionHook -> {
            banService.banUserWithNotification(userBeingBanned, reason, kickingUser, model.getEvent().getGuild(), messageDeletionDuration)
                    .thenCompose((future) -> FutureUtils.toSingleFutureGeneric(interactionService.sendMessageToInteraction(KICK_MODERATION_ACTION_MODAL_RESPONSE_TEMPLATE, new Object(), model.getEvent().getInteraction().getHook())))
                    .thenAccept(unused -> {
                log.info("Kicked user {} from server {}. Performed by user {}.", userBeingBanned.getUserId(), kickingUser.getServerId(), kickingUser.getUserId());
            }).exceptionally(throwable -> {
                interactionExceptionService.reportExceptionToInteraction(throwable, model, this);
                log.error("Failed to kick user {} from server {}. Performed by user {}.", userBeingBanned.getUserId(), kickingUser.getServerId(), kickingUser.getUserId(), throwable);
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
