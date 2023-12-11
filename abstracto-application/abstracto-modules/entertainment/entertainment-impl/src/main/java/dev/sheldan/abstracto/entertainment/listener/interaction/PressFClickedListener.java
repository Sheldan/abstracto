package dev.sheldan.abstracto.entertainment.listener.interaction;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListener;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListenerModel;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListenerResult;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.entertainment.config.EntertainmentFeatureDefinition;
import dev.sheldan.abstracto.entertainment.exception.AlreadyPressedFException;
import dev.sheldan.abstracto.entertainment.model.PressFPayload;
import dev.sheldan.abstracto.entertainment.model.command.PressFJoinModel;
import dev.sheldan.abstracto.entertainment.model.database.PressF;
import dev.sheldan.abstracto.entertainment.service.EntertainmentServiceBean;
import dev.sheldan.abstracto.entertainment.service.management.PressFManagementService;
import dev.sheldan.abstracto.entertainment.service.management.PressFPresserManagementServiceBean;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class PressFClickedListener implements ButtonClickedListener {

    private static final String PRESS_F_CLICK_RESPONSE_TEMPLATE_KEY = "pressF_join";

    @Autowired
    private PressFPresserManagementServiceBean pressFPresserManagementServiceBean;

    @Autowired
    private PressFManagementService pressFManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private PressFClickedListener self;

    @Override
    public Boolean handlesEvent(ButtonClickedListenerModel model) {
        return EntertainmentServiceBean.PRESS_F_BUTTON_ORIGIN.equals(model.getOrigin());
    }

    @Override
    public ButtonClickedListenerResult execute(ButtonClickedListenerModel model) {
        PressFPayload payload = (PressFPayload) model.getDeserializedPayload();
        Optional<PressF> pressFOptional = pressFManagementService.getPressFById(payload.getPressFId());
        pressFOptional.ifPresent(pressF -> {
            Member presserMember = model.getEvent().getMember();
            AUserInAServer presser = userInServerManagementService.loadOrCreateUser(presserMember);
            Long userInServerId = presser.getUserInServerId();
            if(!pressFPresserManagementServiceBean.didUserAlreadyPress(pressF, presser)) {
                PressFJoinModel joinModel = PressFJoinModel
                        .builder()
                        .messageId(pressF.getMessageId())
                        .memberDisplay(MemberDisplay.fromMember(presserMember))
                        .build();
                interactionService.replyEmbed(PRESS_F_CLICK_RESPONSE_TEMPLATE_KEY, joinModel, model.getEvent().getInteraction()).thenAccept(interactionHook -> {
                    self.persistPresser(payload.getPressFId(), userInServerId);
                    log.info("Send message about pressing to user {} for pressF {}.", presserMember.getIdLong(), payload.getPressFId());
                }).exceptionally(throwable -> {
                    log.error("Failed to send message or persist press user {} in pressF {}.", presserMember.getIdLong(), payload.getPressFId(), throwable);
                    return null;
                });
            } else {
                log.debug("User {} already pressed for pressF {}.", presserMember.getIdLong(), payload.getPressFId());
                throw new AlreadyPressedFException();
            }
        });
        return ButtonClickedListenerResult.ACKNOWLEDGED;
    }

    @Transactional
    public void persistPresser(Long pressFId, Long userInServerId) {
        log.info("Persisting pressing of user {} for pressF {}.", userInServerId, pressFId);
        AUserInAServer presser = userInServerManagementService.loadOrCreateUser(userInServerId);
        Optional<PressF> pressFByIdOptional = pressFManagementService.getPressFById(pressFId);
        pressFByIdOptional.ifPresent(pressF -> {
            pressFPresserManagementServiceBean.addPresser(pressF, presser);
        });
    }

    @Override
    public FeatureDefinition getFeature() {
        return EntertainmentFeatureDefinition.ENTERTAINMENT;
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
