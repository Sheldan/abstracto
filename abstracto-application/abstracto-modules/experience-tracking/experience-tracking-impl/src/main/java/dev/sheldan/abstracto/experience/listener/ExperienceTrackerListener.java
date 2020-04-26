package dev.sheldan.abstracto.experience.listener;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.listener.MessageReceivedListener;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.UserManagementService;
import dev.sheldan.abstracto.experience.config.features.ExperienceFeature;
import dev.sheldan.abstracto.experience.service.AUserExperienceService;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * This {@link MessageReceivedListener} is responsible to execute the {@link AUserExperienceService} in order to track
 * that a certain user has send a message, and experience should be awarded.
 */
@Component
public class ExperienceTrackerListener implements MessageReceivedListener {

    @Autowired
    private AUserExperienceService userExperienceService;

    @Autowired
    private UserManagementService userManagementService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void execute(Message message) {
        AUserInAServer cause = userManagementService.loadUser(message.getMember());
        userExperienceService.addExperience(cause);
    }

    @Override
    public FeatureEnum getFeature() {
        return ExperienceFeature.EXPERIENCE;
    }
}
