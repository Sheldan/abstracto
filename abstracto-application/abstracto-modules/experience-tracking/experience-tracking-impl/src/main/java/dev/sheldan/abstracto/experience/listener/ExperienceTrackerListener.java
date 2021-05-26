package dev.sheldan.abstracto.experience.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncMessageReceivedListener;
import dev.sheldan.abstracto.core.listener.sync.jda.MessageReceivedListener;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.listener.MessageReceivedModel;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.experience.config.ExperienceFeatureDefinition;
import dev.sheldan.abstracto.experience.service.AUserExperienceService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
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
    public DefaultListenerResult execute(MessageReceivedModel model) {
        Message message = model.getMessage();
        if(!message.isFromGuild() || message.isWebhookMessage() || message.getType().isSystem() || message.getAuthor().isBot()) {
            return DefaultListenerResult.IGNORED;
        }
        if(userExperienceService.experienceGainEnabledInChannel(message.getChannel())) {
            AUserInAServer cause = userInServerManagementService.loadOrCreateUser(model.getServerId(), model.getMessage().getAuthor().getIdLong());
            userExperienceService.addExperience(cause);
            return DefaultListenerResult.PROCESSED;
        } else  {
            return DefaultListenerResult.IGNORED;
        }
    }

    @Override
    public FeatureDefinition getFeature() {
        return ExperienceFeatureDefinition.EXPERIENCE;
    }

}
