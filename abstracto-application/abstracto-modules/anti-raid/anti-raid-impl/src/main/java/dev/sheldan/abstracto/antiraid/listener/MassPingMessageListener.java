package dev.sheldan.abstracto.antiraid.listener;

import dev.sheldan.abstracto.antiraid.config.AntiRaidFeatureDefinition;
import dev.sheldan.abstracto.antiraid.service.MassPingService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncMessageReceivedListener;
import dev.sheldan.abstracto.core.models.listener.MessageReceivedModel;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MassPingMessageListener implements AsyncMessageReceivedListener {

    @Autowired
    private MassPingService massPingService;

    @Autowired
    private Tracer tracer;

    @Override
    public DefaultListenerResult execute(MessageReceivedModel model) {
        Span newSpan = tracer.nextSpan().name("mass-ping-filter");
        try (Tracer.SpanInScope ws = this.tracer.withSpan(newSpan.start())) {
            Message message = model.getMessage();
            if (message.getAuthor().isBot() || message.isWebhookMessage() || !message.isFromGuild() || !message.isFromType(ChannelType.TEXT)) {
                newSpan.end();
                return DefaultListenerResult.IGNORED;
            }
            massPingService.processMessage(message).whenComplete((unused, throwable) -> {
                newSpan.end();
            });
        }
        return DefaultListenerResult.PROCESSED;
    }

    @Override
    public FeatureDefinition getFeature() {
        return AntiRaidFeatureDefinition.ANTI_RAID;
    }
}
