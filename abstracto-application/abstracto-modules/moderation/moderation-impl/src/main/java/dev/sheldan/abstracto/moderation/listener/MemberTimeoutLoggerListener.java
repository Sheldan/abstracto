package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncMemberTimeoutUpdatedListener;
import dev.sheldan.abstracto.core.models.listener.MemberTimeoutUpdatedModel;
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.model.template.command.MuteLogModel;
import dev.sheldan.abstracto.moderation.service.MuteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@Slf4j
public class MemberTimeoutLoggerListener implements AsyncMemberTimeoutUpdatedListener {

    @Autowired
    private MuteService muteService;

    @Override
    public DefaultListenerResult execute(MemberTimeoutUpdatedModel model) {
        log.info("Notifying about timeout of user {} in guild {}.", model.getMutedUser().getUserId(), model.getServerId());
        if(model.getMutingUser().getUserId() == model.getGuild().getSelfMember().getIdLong()) {
            log.info("Skipping logging timeout event about user {} in guild {}, because it was done by us.", model.getMutedUser().getUserId(), model.getGuild().getIdLong());
            return DefaultListenerResult.IGNORED;
        }
        MemberDisplay mutedMemberDisplay = model.getMutedMember() != null ? MemberDisplay.fromMember(model.getMutedMember()) : MemberDisplay.fromServerUser(model.getMutedUser());
        MemberDisplay mutingMemberDisplay = model.getMutingMember() != null ? MemberDisplay.fromMember(model.getMutingMember()) : MemberDisplay.fromServerUser(model.getMutingUser());
        Duration duration = null;
        if(model.getNewTimeout() != null) {
            duration = Duration.between(Instant.now(), model.getNewTimeout());
        }
        MuteLogModel muteLogModel = MuteLogModel
                .builder()
                .muteTargetDate(model.getNewTimeout() != null ? model.getNewTimeout().toInstant() : null)
                .oldMuteTargetDate(model.getOldTimeout() != null ? model.getOldTimeout().toInstant() : null)
                .mutingMember(mutingMemberDisplay)
                .mutedMember(mutedMemberDisplay)
                .duration(duration)
                .reason(model.getReason())
                .build();
        muteService.sendMuteLogMessage(muteLogModel, model.getServerId());
        return DefaultListenerResult.PROCESSED;
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.MUTING;
    }
}
