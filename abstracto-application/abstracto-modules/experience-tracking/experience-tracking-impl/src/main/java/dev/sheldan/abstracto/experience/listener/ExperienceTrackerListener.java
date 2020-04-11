package dev.sheldan.abstracto.experience.listener;

import dev.sheldan.abstracto.core.listener.MessageReceivedListener;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.UserManagementService;
import dev.sheldan.abstracto.experience.config.ExperienceFeatures;
import dev.sheldan.abstracto.experience.service.ExperienceTrackerService;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ExperienceTrackerListener implements MessageReceivedListener {

    @Autowired
    private ExperienceTrackerService experienceTrackerService;

    @Autowired
    private UserManagementService userManagementService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void execute(Message message) {
        AUserInAServer cause = userManagementService.loadUser(message.getMember());
        experienceTrackerService.addExperience(cause);
    }

    @Override
    public String getFeature() {
        return ExperienceFeatures.EXPERIENCE;
    }
}
