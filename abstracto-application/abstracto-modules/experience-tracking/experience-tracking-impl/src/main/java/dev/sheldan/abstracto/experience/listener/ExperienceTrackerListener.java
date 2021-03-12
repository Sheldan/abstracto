package dev.sheldan.abstracto.experience.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncMessageReceivedListener;
import dev.sheldan.abstracto.core.listener.sync.jda.MessageReceivedListener;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.experience.config.ExperienceFeatureDefinition;
import dev.sheldan.abstracto.experience.service.AUserExperienceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This {@link MessageReceivedListener listener} is responsible to execute the {@link AUserExperienceService service} in order to track
 * that a certain user has send a message, and experience should be awarded.
 */
@Component
@Slf4j
public class ExperienceTrackerListener implements AsyncMessageReceivedListener {

    @Autowired
    private AUserExperienceService userExperienceService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Override
    public void execute(CachedMessage message) {
        AUserInAServer cause = userInServerManagementService.loadOrCreateUser(message.getServerId(), message.getAuthor().getAuthorId());
        userExperienceService.addExperience(cause);
    }

    @Override
    public FeatureDefinition getFeature() {
        return ExperienceFeatureDefinition.EXPERIENCE;
    }

}
