package dev.sheldan.abstracto.invitefilter.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncMessageReceivedListener;
import dev.sheldan.abstracto.core.models.listener.MessageReceivedModel;
import dev.sheldan.abstracto.invitefilter.config.InviteFilterFeatureDefinition;
import dev.sheldan.abstracto.invitefilter.service.InviteLinkFilterService;
import dev.sheldan.abstracto.invitefilter.service.InviteLinkFilterServiceBean;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class InviteLinkFilterListener implements AsyncMessageReceivedListener {

    @Autowired
    private InviteLinkFilterService inviteLinkFilterService;

    @Autowired
    private InviteLinkFilterServiceBean filterServiceBean;

    @Autowired
    private Tracer tracer;

    @Override
    public FeatureDefinition getFeature() {
        return InviteFilterFeatureDefinition.INVITE_FILTER;
    }

    @Override
    public DefaultListenerResult execute(MessageReceivedModel model) {
        Span newSpan = tracer.nextSpan().name("invite-filter");
        try (Tracer.SpanInScope ws = this.tracer.withSpan(newSpan.start())) {
            Message message = model.getMessage();

            if (!message.isFromGuild() || message.isWebhookMessage() || message.getType().isSystem()) {
                newSpan.end();
                return DefaultListenerResult.IGNORED;
            }

            List<String> foundInvites = inviteLinkFilterService.findInvitesInMessage(message);

            if (foundInvites.isEmpty()) {
                newSpan.end();
                return DefaultListenerResult.IGNORED;
            }

            if (!inviteLinkFilterService.isInviteFilterActiveInChannel(message.getChannel())) {
                newSpan.end();
                return DefaultListenerResult.IGNORED;
            }

            if (inviteLinkFilterService.isMemberImmuneAgainstInviteFilter(message.getMember())) {
                log.info("Not checking for invites in message, because author {} in channel {} in guild {} is immune against invite filter.",
                        message.getMember().getIdLong(), message.getGuild().getIdLong(), message.getChannel().getIdLong());
                newSpan.end();
                return DefaultListenerResult.IGNORED;
            }

            // only to reduce code duplication, the interface is too concrete
            filterServiceBean.resolveAndCheckInvites(message, foundInvites).whenComplete((unused, throwable) -> {
                newSpan.end();
            });

            return DefaultListenerResult.PROCESSED;
        }
    }
}
