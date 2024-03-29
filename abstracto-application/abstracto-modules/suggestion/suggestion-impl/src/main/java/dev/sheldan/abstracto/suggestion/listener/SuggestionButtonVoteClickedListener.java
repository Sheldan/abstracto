package dev.sheldan.abstracto.suggestion.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListenerResult;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListener;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListenerModel;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.suggestion.config.SuggestionFeatureDefinition;
import dev.sheldan.abstracto.suggestion.model.template.SuggestionButtonPayload;
import dev.sheldan.abstracto.suggestion.service.SuggestionServiceBean;
import dev.sheldan.abstracto.suggestion.service.SuggestionVoteService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SuggestionButtonVoteClickedListener implements ButtonClickedListener {

    @Autowired
    private SuggestionVoteService suggestionVoteService;

    @Autowired
    private InteractionService interactionService;

    public static final String VOTE_REMOVED_TEMPLATE_KEY = "suggestion_vote_removed_notification";
    public static final String VOTE_AGREEMENT_TEMPLATE_KEY = "suggestion_vote_agreement_notification";
    public static final String VOTE_DISAGREEMENT_TEMPLATE_KEY = "suggestion_vote_disagreement_notification";

    @Override
    public ButtonClickedListenerResult execute(ButtonClickedListenerModel model) {
        ButtonInteractionEvent event = model.getEvent();
        SuggestionButtonPayload payload = (SuggestionButtonPayload) model.getDeserializedPayload();
        suggestionVoteService.upsertSuggestionVote(event.getMember(), payload.getDecision(), payload.getSuggestionId());
        ButtonInteraction buttonInteraction = model.getEvent().getInteraction();
        String templateToUse;
        switch (payload.getDecision()) {
            case AGREE:
                templateToUse = VOTE_AGREEMENT_TEMPLATE_KEY;
                break;
            case DISAGREE:
                templateToUse = VOTE_DISAGREEMENT_TEMPLATE_KEY;
                break;
            default:
            case REMOVE_VOTE:
                templateToUse = VOTE_REMOVED_TEMPLATE_KEY;
        }
        FutureUtils.toSingleFutureGeneric(interactionService.sendMessageToInteraction(templateToUse, new Object(), buttonInteraction.getHook()))
        .thenAccept(unused -> log.info("Notified user {} about vote action in suggestion {} in server {}.",
                model.getEvent().getMember().getIdLong(), payload.getSuggestionId(), payload.getServerId()));

        return ButtonClickedListenerResult.ACKNOWLEDGED;
    }

    @Override
    public Boolean handlesEvent(ButtonClickedListenerModel model) {
        return SuggestionServiceBean.SUGGESTION_VOTE_ORIGIN.equals(model.getOrigin()) && model.getEvent().isFromGuild();
    }

    @Override
    public FeatureDefinition getFeature() {
        return SuggestionFeatureDefinition.SUGGEST;
    }

    @Override
    public Integer getPriority() {
        return ListenerPriority.MEDIUM;
    }
}
