package dev.sheldan.abstracto.suggestion.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.menu.listener.StringSelectMenuListener;
import dev.sheldan.abstracto.core.interaction.menu.listener.StringSelectMenuListenerModel;
import dev.sheldan.abstracto.core.interaction.menu.listener.StringSelectMenuListenerResult;
import dev.sheldan.abstracto.core.models.template.display.MemberNameDisplay;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.suggestion.config.SuggestionFeatureDefinition;
import dev.sheldan.abstracto.suggestion.model.database.PollType;
import dev.sheldan.abstracto.suggestion.model.template.PollDecisionNotificationModel;
import dev.sheldan.abstracto.suggestion.model.payload.ServerPollSelectionMenuPayload;
import dev.sheldan.abstracto.suggestion.service.PollService;
import dev.sheldan.abstracto.suggestion.service.PollServiceBean;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ServerPollDecisionListener implements StringSelectMenuListener {

    @Autowired
    private PollService pollService;

    @Autowired
    private InteractionService interactionService;

    private static final String POLL_DECISION_NOTIFICATION = "poll_decision_notification";

    @Override
    public StringSelectMenuListenerResult execute(StringSelectMenuListenerModel model) {
        StringSelectInteractionEvent event = model.getEvent();
        ServerPollSelectionMenuPayload payload = (ServerPollSelectionMenuPayload) model.getDeserializedPayload();
        PollDecisionNotificationModel notificationModel = PollDecisionNotificationModel
                .builder()
                .chosenValues(event.getValues())
                .pollId(payload.getPollId())
                .memberNameDisplay(MemberNameDisplay.fromMember(event.getMember()))
                .serverId(model.getServerId())
                .build();
        pollService.setDecisionsInPollTo(event.getMember(), event.getValues(), payload.getPollId(), PollType.STANDARD)
                .thenCompose(unused -> FutureUtils.toSingleFutureGeneric(interactionService.sendMessageToInteraction(POLL_DECISION_NOTIFICATION, notificationModel, event.getInteraction().getHook())))
                .exceptionally(throwable -> {
                    log.info("Failed to member {} in server {} about decision in poll {}.", event.getMember().getIdLong(), model.getServerId(), payload.getPollId(), throwable);
                    return null;
                }).thenAccept(unused1 -> {
                    log.info("Notified member {} in server {} about decision in poll {}.", event.getMember().getIdLong(), model.getServerId(), payload.getPollId());
                });
        return StringSelectMenuListenerResult.ACKNOWLEDGED;
    }


    @Override
    public Boolean handlesEvent(StringSelectMenuListenerModel model) {
        return model.getOrigin().equals(PollServiceBean.SERVER_POLL_SELECTION_MENU_ORIGIN);
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
