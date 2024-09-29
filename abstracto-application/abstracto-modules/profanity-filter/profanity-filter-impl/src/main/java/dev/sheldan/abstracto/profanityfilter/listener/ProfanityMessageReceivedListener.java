package dev.sheldan.abstracto.profanityfilter.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncMessageReceivedListener;
import dev.sheldan.abstracto.core.models.database.ProfanityRegex;
import dev.sheldan.abstracto.core.models.listener.MessageReceivedModel;
import dev.sheldan.abstracto.core.service.ProfanityService;
import dev.sheldan.abstracto.profanityfilter.config.ProfanityFilterFeatureDefinition;
import dev.sheldan.abstracto.profanityfilter.service.ProfanityFilterService;
import dev.sheldan.abstracto.profanityfilter.service.ProfanityFilterServiceBean;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class ProfanityMessageReceivedListener implements AsyncMessageReceivedListener {

    @Autowired
    private ProfanityService profanityService;

    @Autowired
    private ProfanityFilterService profanityFilterService;

    @Autowired
    private ProfanityFilterServiceBean profanityFilterServiceBean;

    @Autowired
    private Tracer tracer;

    @Override
    public DefaultListenerResult execute(MessageReceivedModel model) {
        Span newSpan = tracer.nextSpan().name("profanity-filter");
        try (Tracer.SpanInScope ws = this.tracer.withSpan(newSpan.start())) {
            Message message = model.getMessage();
            if (message.isWebhookMessage() || message.getType().isSystem() || !message.isFromGuild()) {
                newSpan.end();
                return DefaultListenerResult.IGNORED;
            }

            if (profanityFilterService.isImmuneAgainstProfanityFilter(message.getMember())) {
                log.debug("Not checking for profanities in message, because author {} in channel {} in guild {} is immune against profanity filter.",
                        message.getMember().getIdLong(), message.getGuild().getIdLong(), message.getChannel().getIdLong());
                newSpan.end();
                return DefaultListenerResult.IGNORED;
            }

            Long serverId = model.getServerId();
            Optional<ProfanityRegex> potentialProfanityGroup = profanityService.getProfanityRegex(message.getContentRaw(), serverId);
            if (potentialProfanityGroup.isPresent()) {
                ProfanityRegex foundProfanityGroup = potentialProfanityGroup.get();
                profanityFilterServiceBean.handleProfaneMessage(message, foundProfanityGroup).whenComplete((unused, throwable) -> {
                    newSpan.end();
                });
                return DefaultListenerResult.PROCESSED;
            }
        }
        return DefaultListenerResult.IGNORED;
    }

    @Override
    public FeatureDefinition getFeature() {
        return ProfanityFilterFeatureDefinition.PROFANITY_FILTER;
    }
}
