package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncJoinListener;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.listener.MemberJoinModel;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.service.MuteService;
import dev.sheldan.abstracto.moderation.service.management.MuteManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JoinMuteListener implements AsyncJoinListener {

    @Autowired
    private MuteManagementService muteManagementService;

    @Autowired
    private MuteService muteService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Override
    public DefaultListenerResult execute(MemberJoinModel model) {
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(model.getServerId(), model.getJoiningUser().getUserId());
        if(muteManagementService.hasActiveMute(aUserInAServer)) {
            log.info("Re-muting user {} which joined the server {}, because the mute has not ended yet.", model.getJoiningUser().getUserId(), model.getServerId());
            muteService.applyMuteRole(aUserInAServer);
        }
        return DefaultListenerResult.PROCESSED;
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.MUTING;
    }

}
