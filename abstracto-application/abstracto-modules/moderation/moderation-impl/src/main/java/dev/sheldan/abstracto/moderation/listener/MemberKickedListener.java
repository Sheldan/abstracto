package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncMemberKickedListener;
import dev.sheldan.abstracto.core.models.listener.MemberKickedModel;
import dev.sheldan.abstracto.core.models.template.display.UserDisplay;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.model.template.command.KickLogModel;
import dev.sheldan.abstracto.moderation.service.KickService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MemberKickedListener implements AsyncMemberKickedListener {

    @Autowired
    private KickService kickService;

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.MODERATION;
    }

    @Override
    public DefaultListenerResult execute(MemberKickedModel eventModel) {
        log.info("Notifying about kicked of user {} in guild {}.", eventModel.getKickedServerUser().getUserId(), eventModel.getServerId());
        if(eventModel.getKickingServerUser().getUserId() == eventModel.getGuild().getJDA().getSelfUser().getIdLong()) {
            log.info("Skipping logging kicked event about user {} in guild {}, because it was done by us.", eventModel.getKickedServerUser().getUserId(), eventModel.getGuild().getIdLong());
            return DefaultListenerResult.IGNORED;
        }
        KickLogModel model = KickLogModel
                .builder()
                .kickedUser(eventModel.getKickedUser() != null ? UserDisplay.fromUser(eventModel.getKickedUser()) : UserDisplay.fromId(eventModel.getKickedServerUser().getUserId()))
                .kickingUser(eventModel.getKickingUser() != null ? UserDisplay.fromUser(eventModel.getKickingUser()) : UserDisplay.fromServerUser(eventModel.getKickingServerUser()))
                .reason(eventModel.getReason())
                .build();
        kickService.sendKicklog(model, eventModel.getServerId());
        return DefaultListenerResult.PROCESSED;
    }
}
