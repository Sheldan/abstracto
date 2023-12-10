package dev.sheldan.abstracto.giveaway.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListener;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListenerModel;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListenerResult;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.giveaway.config.GiveawayFeatureDefinition;
import dev.sheldan.abstracto.giveaway.exception.GiveawayNotFoundException;
import dev.sheldan.abstracto.giveaway.model.JoinGiveawayPayload;
import dev.sheldan.abstracto.giveaway.model.database.Giveaway;
import dev.sheldan.abstracto.giveaway.service.GiveawayService;
import dev.sheldan.abstracto.giveaway.service.GiveawayServiceBean;
import dev.sheldan.abstracto.giveaway.service.management.GiveawayManagementService;
import dev.sheldan.abstracto.giveaway.service.management.GiveawayParticipantManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class GiveawayJoinListener implements ButtonClickedListener {

    private static final String GIVEAWAY_JOIN_RESPONSE_TEMPLATE_KEY = "giveaway_join_response";
    private static final String GIVEAWAY_ALREADY_JOINED_RESPONSE_TEMPLATE_KEY = "giveaway_already_joined_response";

    @Autowired
    private GiveawayManagementService giveawayManagementService;

    @Autowired
    private GiveawayService giveawayService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private GiveawayParticipantManagementService giveawayParticipantManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Override
    public ButtonClickedListenerResult execute(ButtonClickedListenerModel model) {
        JoinGiveawayPayload payload = (JoinGiveawayPayload) model.getDeserializedPayload();
        Optional<Giveaway> optionalGiveaway = giveawayManagementService.loadGiveawayById(payload.getGiveawayId(), payload.getServerId());
        if(optionalGiveaway.isPresent()) {
            Giveaway giveaway = optionalGiveaway.get();
            AUserInAServer user = userInServerManagementService.loadOrCreateUser(model.getEvent().getMember());
            Long joiningUserId = model.getEvent().getMember().getIdLong();
            if(!giveawayParticipantManagementService.userIsAlreadyParticipating(giveaway, user)) {
                log.info("Adding user {} to giveaway {}.", joiningUserId, payload.getGiveawayId());
                giveawayService.addGiveawayParticipant(giveaway, model.getEvent().getMember(), model.getEvent().getMessageChannel())
                        .thenAccept(unused -> {
                            log.info("Notified user {} in giveaway {} join event.", joiningUserId, payload.getGiveawayId());
                            interactionService.replyEmbed(GIVEAWAY_JOIN_RESPONSE_TEMPLATE_KEY, model.getEvent());
                        }).exceptionally(throwable -> {
                            log.error("Failed to add {} to giveaway {}.", joiningUserId, payload.getGiveawayId(), throwable);
                            return null;
                        });
            } else {
                log.info("User {} was already part of giveaway {}.", joiningUserId, payload.getGiveawayId());
                interactionService.replyEmbed(GIVEAWAY_ALREADY_JOINED_RESPONSE_TEMPLATE_KEY, model.getEvent());
            }
            return ButtonClickedListenerResult.ACKNOWLEDGED;
        } else {
            throw new GiveawayNotFoundException();
        }
    }

    @Override
    public Boolean handlesEvent(ButtonClickedListenerModel model) {
        return GiveawayServiceBean.GIVEAWAY_JOIN_ORIGIN.equals(model.getOrigin());
    }

    @Override
    public FeatureDefinition getFeature() {
        return GiveawayFeatureDefinition.GIVEAWAY;
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
