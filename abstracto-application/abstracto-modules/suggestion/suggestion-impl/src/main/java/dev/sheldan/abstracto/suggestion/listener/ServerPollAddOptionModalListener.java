package dev.sheldan.abstracto.suggestion.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.modal.listener.ModalInteractionListener;
import dev.sheldan.abstracto.core.interaction.modal.listener.ModalInteractionListenerModel;
import dev.sheldan.abstracto.core.interaction.modal.listener.ModalInteractionListenerResult;
import dev.sheldan.abstracto.core.models.template.display.MemberNameDisplay;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.suggestion.config.SuggestionFeatureDefinition;
import dev.sheldan.abstracto.suggestion.exception.PollOptionAlreadyExistsException;
import dev.sheldan.abstracto.suggestion.model.database.Poll;
import dev.sheldan.abstracto.suggestion.model.database.PollType;
import dev.sheldan.abstracto.suggestion.model.payload.PollAddOptionModalPayload;
import dev.sheldan.abstracto.suggestion.model.template.PollAddOptionNotificationModel;
import dev.sheldan.abstracto.suggestion.service.PollService;
import dev.sheldan.abstracto.suggestion.service.management.PollManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class ServerPollAddOptionModalListener implements ModalInteractionListener {

    @Autowired
    private PollService pollService;

    @Autowired
    private ServerPollAddOptionModalListener self;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private PollManagementService pollManagementService;

    private static final String POLL_ADD_OPTION_NOTIFICATION = "poll_add_option_notification";

    @Override
    public ModalInteractionListenerResult execute(ModalInteractionListenerModel model) {
        PollAddOptionModalPayload payload = (PollAddOptionModalPayload) model.getDeserializedPayload();
        log.info("Handling modal event to add options to poll {} in server {} by member {}.", payload.getPollId(), payload.getServerId(), model.getEvent().getMember().getIdLong());
        String labelContent = model
                .getEvent()
                .getValues()
                .stream()
                .filter(modalMapping -> modalMapping.getId().equals(payload.getLabelInputComponentId()))
                .map(ModalMapping::getAsString)
                .findFirst()
                .orElse(null);

        Poll affectedPoll = pollManagementService.getPollByPollId(payload.getPollId(), payload.getServerId(), PollType.STANDARD);
        if(affectedPoll.getOptions().stream().anyMatch(pollOption -> pollOption.getLabel().equals(labelContent))) {
            throw new PollOptionAlreadyExistsException();
        }

        String descriptionContent = model
                .getEvent()
                .getValues()
                .stream()
                .filter(modalMapping -> modalMapping.getId().equals(payload.getDescriptionInputComponentId()))
                .map(ModalMapping::getAsString)
                .findFirst()
                .orElse(null);
        PollAddOptionNotificationModel pollAddOptionNotificationModel = PollAddOptionNotificationModel
                .builder()
                .description(descriptionContent)
                .memberNameDisplay(MemberNameDisplay.fromMember(model.getEvent().getMember()))
                .label(labelContent)
                .value(labelContent)
                .pollId(payload.getPollId())
                .serverId(payload.getServerId())
                .build();


        model.getEvent().deferReply(true).queue(interactionHook -> {
            self.updatePoll(model, payload, labelContent, descriptionContent);
            FutureUtils.toSingleFutureGeneric(interactionService.sendMessageToInteraction(POLL_ADD_OPTION_NOTIFICATION, pollAddOptionNotificationModel, model.getEvent().getInteraction().getHook())).thenAccept(unused -> {
                log.info("Send notification about successfully adding option to poll {} in server {} to member {}", payload.getPollId(), payload.getServerId(), model.getEvent().getMember().getIdLong());
            }).exceptionally(throwable -> {
                log.info("Failed to send notification about adding option to poll {} in server {} to member {}", payload.getPollId(), payload.getServerId(), model.getEvent().getMember().getIdLong());
                return null;
            });
        }, throwable -> {
            log.error("Failed to acknowledge modal interaction for poll add option modal listener in guild {}.", model.getServerId(), throwable);
        });

        return ModalInteractionListenerResult.ACKNOWLEDGED;
    }

    @Transactional
    public void updatePoll(ModalInteractionListenerModel model, PollAddOptionModalPayload payload, String labelContent, String descriptionContent) {
        pollService.addOptionToServerPoll(payload.getPollId(), payload.getServerId(), model.getEvent().getMember(), labelContent, descriptionContent).thenAccept(unused -> {
            log.info("Added option to poll {} in server {} by member {}.", payload.getPollId(), payload.getServerId(), model.getEvent().getMember().getIdLong());
        }).exceptionally(throwable -> {
            log.error("Failed to add option to poll {} in server {} by member {}.",
                    payload.getPollId(), payload.getServerId(), model.getEvent().getMember().getIdLong(), throwable);
            return null;
        });
    }

    @Override
    public FeatureDefinition getFeature() {
        return SuggestionFeatureDefinition.POLL;
    }

    @Override
    public Integer getPriority() {
        return ListenerPriority.MEDIUM;
    }

    @Override
    public Boolean handlesEvent(ModalInteractionListenerModel model) {
        return ServerPollAddOptionButtonListener.SERVER_POLL_ADD_OPTION_MODAL_ORIGIN.equals(model.getOrigin());
    }
}
