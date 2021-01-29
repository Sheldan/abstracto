package dev.sheldan.abstracto.moderation.listener.async;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncJoinListener;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.moderation.config.features.ModerationFeatures;
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
    public void execute(ServerUser serverUser) {
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(serverUser.getServerId(), serverUser.getUserId());
        if(muteManagementService.hasActiveMute(aUserInAServer)) {
            log.info("Re-muting user {} which joined the server {}, because the mute has not ended yet.", serverUser.getUserId(), serverUser.getServerId());
            muteService.applyMuteRole(aUserInAServer);
        }
    }

    @Override
    public FeatureEnum getFeature() {
        return ModerationFeatures.MUTING;
    }

}
