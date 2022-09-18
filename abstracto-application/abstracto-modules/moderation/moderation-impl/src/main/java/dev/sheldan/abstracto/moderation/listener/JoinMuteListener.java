package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncJoinListener;
import dev.sheldan.abstracto.core.models.listener.MemberJoinModel;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.model.database.Mute;
import dev.sheldan.abstracto.moderation.service.management.MuteManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class JoinMuteListener implements AsyncJoinListener {

    @Autowired
    private MuteManagementService muteManagementService;

    @Autowired
    private MemberService memberService;

    @Override
    public DefaultListenerResult execute(MemberJoinModel model) {
        Optional<Mute> optionalMute = muteManagementService.getAMuteOfOptional(model.getMember());
        if(optionalMute.isPresent()) {
            log.info("Re-muting user {} which joined the server {}, because the mute has not ended yet.", model.getJoiningUser().getUserId(), model.getServerId());
            Mute mute = optionalMute.get();
            memberService.timeoutUser(model.getMember(), mute.getMuteTargetDate());
        }
        return DefaultListenerResult.PROCESSED;
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.MUTING;
    }

}
