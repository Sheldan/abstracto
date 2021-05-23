package dev.sheldan.abstracto.invitefilter.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncMessageUpdatedListener;
import dev.sheldan.abstracto.core.models.listener.MessageUpdatedModel;
import dev.sheldan.abstracto.invitefilter.config.InviteFilterFeatureDefinition;
import dev.sheldan.abstracto.invitefilter.service.InviteLinkFilterService;
import dev.sheldan.abstracto.invitefilter.service.InviteLinkFilterServiceBean;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class InviteLinkFilterEditedListener implements AsyncMessageUpdatedListener {

    @Autowired
    private InviteLinkFilterService inviteLinkFilterService;

    @Autowired
    private InviteLinkFilterServiceBean filterServiceBean;

    @Override
    public FeatureDefinition getFeature() {
        return InviteFilterFeatureDefinition.INVITE_FILTER;
    }

    @Override
    public DefaultListenerResult execute(MessageUpdatedModel model) {
        Message message = model.getAfter();

        if(!message.isFromGuild() || message.isWebhookMessage() || message.getType().isSystem()) {
            return DefaultListenerResult.IGNORED;
        }

        List<String> foundInvites = inviteLinkFilterService.findInvitesInMessage(message);

        if(foundInvites.isEmpty()){
            return DefaultListenerResult.IGNORED;
        }

        if(!inviteLinkFilterService.isInviteFilterActiveInChannel(message.getChannel())) {
            return DefaultListenerResult.IGNORED;
        }

        if(inviteLinkFilterService.isMemberImmuneAgainstInviteFilter(message.getMember())) {
            log.info("Not checking for invites in message, because author {} in channel {} in guild {} is immune against invite filter.",
                    message.getMember().getIdLong(), message.getGuild().getIdLong(), message.getChannel().getIdLong());
            return DefaultListenerResult.IGNORED;
        }

        // only to reduce code duplication, the interface is too concrete
        filterServiceBean.resolveAndCheckInvites(message, foundInvites);

        return DefaultListenerResult.PROCESSED;
    }
}
