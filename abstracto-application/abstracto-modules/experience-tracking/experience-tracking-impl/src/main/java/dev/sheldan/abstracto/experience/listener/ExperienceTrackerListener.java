package dev.sheldan.abstracto.experience.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncMessageReceivedListener;
import dev.sheldan.abstracto.core.listener.sync.jda.MessageReceivedListener;
import dev.sheldan.abstracto.core.models.listener.MessageReceivedModel;
import dev.sheldan.abstracto.experience.config.ExperienceFeatureDefinition;
import dev.sheldan.abstracto.experience.service.AUserExperienceService;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
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
    private Tracer tracer;

    @Override
    public DefaultListenerResult execute(MessageReceivedModel model) {
        Span newSpan = tracer.nextSpan().name("experience-tracker");
        try (Tracer.SpanInScope ws = this.tracer.withSpan(newSpan.start())) {
            Message message = model.getMessage();
            if(!message.isFromGuild() || message.isWebhookMessage() || message.getType().isSystem() || message.getAuthor().isBot()) {
                newSpan.end();
                return DefaultListenerResult.IGNORED;
            }
            if(userExperienceService.experienceGainEnabledInChannel(message.getChannel())) {
                userExperienceService.addExperience(message.getMember(), model.getMessage()).whenComplete((unused, throwable) -> {
                    newSpan.end();
                });
                return DefaultListenerResult.PROCESSED;
            } else  {
                return DefaultListenerResult.IGNORED;
            }
        }
    }

    @Override
    public FeatureDefinition getFeature() {
        return ExperienceFeatureDefinition.EXPERIENCE;
    }

}
