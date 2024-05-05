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
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.core.utils.ParseUtils;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.model.interaction.ModerationActionMutePayload;
import dev.sheldan.abstracto.moderation.service.MuteService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static dev.sheldan.abstracto.moderation.command.Mute.MUTE_NOTIFICATION_NOT_POSSIBLE_TEMPLATE_KEY;
import static dev.sheldan.abstracto.moderation.model.MuteResult.NOTIFICATION_FAILED;

@Component
@Slf4j
public class MuteModerationActionModalListener implements ModalInteractionListener {

    private static final String MUTE_MODERATION_ACTION_MODAL_RESPONSE_TEMPLATE = "moderationAction_mute_response";

    @Autowired
    private MuteService muteService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private InteractionExceptionService interactionExceptionService;

    @Override
    public Boolean handlesEvent(ModalInteractionListenerModel model) {
        return MuteModerationActionListener.MUTE_MODAL_ORIGIN.equals(model.getOrigin());
    }

    @Override
    public ModalInteractionListenerResult execute(ModalInteractionListenerModel model) {
        ModerationActionMutePayload payload = (ModerationActionMutePayload) model.getDeserializedPayload();
        ServerUser userBeingMuted = ServerUser
                .builder()
                .userId(payload.getMutedUserId())
                .serverId(payload.getServerId())
                .build();

        ServerUser mutingUser = ServerUser.fromMember(model.getEvent().getMember());
        String reason = model
                .getEvent()
                .getValues()
                .stream()
                .filter(modalMapping -> modalMapping.getId().equals(payload.getReasonInputId()))
                .map(ModalMapping::getAsString)
                .findFirst()
                .orElse(null);

        String duration = model
                .getEvent()
                .getValues()
                .stream()
                .filter(modalMapping -> modalMapping.getId().equals(payload.getDurationInputId()))
                .map(ModalMapping::getAsString)
                .findFirst()
                .orElse(null);
        Duration muteDuration;
        if(duration != null) {
            muteDuration = ParseUtils.parseDuration(duration.trim());
        } else {
            muteDuration = Duration.ofDays(Member.MAX_TIME_OUT_LENGTH);
        }
        ServerChannelMessage serverChannelMessage = ServerChannelMessage.fromMessage(model.getEvent().getMessage());
        log.debug("Handling mute moderation action modal interaction by user {} in server {}.", mutingUser.getUserId(), mutingUser.getServerId());
        model.getEvent().deferReply(true).queue(interactionHook -> {
            muteService.muteMemberWithLog(userBeingMuted, mutingUser, reason, muteDuration, model.getEvent().getGuild(), serverChannelMessage)
                    .thenCompose((future) -> {
                        if(future == NOTIFICATION_FAILED) {
                            return FutureUtils.toSingleFutureGeneric(interactionService.sendMessageToInteraction(MUTE_NOTIFICATION_NOT_POSSIBLE_TEMPLATE_KEY, new Object(), model.getEvent().getInteraction().getHook()));
                        } else {
                            return FutureUtils.toSingleFutureGeneric(interactionService.sendMessageToInteraction(MUTE_MODERATION_ACTION_MODAL_RESPONSE_TEMPLATE, new Object(), model.getEvent().getInteraction().getHook()));
                        }
                    })
                    .thenAccept(unused -> {
                log.info("Muted user {} in server {}. Performed by user {}.", userBeingMuted.getUserId(), mutingUser.getServerId(), mutingUser.getUserId());
            }).exceptionally(throwable -> {
                interactionExceptionService.reportExceptionToInteraction(throwable, model, this);
                log.error("Failed to mute user {} in server {}. Performed by user {}.", userBeingMuted.getUserId(), mutingUser.getServerId(), mutingUser.getUserId(), throwable);
                return null;
            });
        });

        return ModalInteractionListenerResult.ACKNOWLEDGED;
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.MUTING;
    }

    @Override
    public Integer getPriority() {
        return ListenerPriority.MEDIUM;
    }
}
