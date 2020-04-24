package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.listener.JoinListener;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.moderation.config.ModerationFeatures;
import dev.sheldan.abstracto.moderation.service.MuteService;
import dev.sheldan.abstracto.moderation.service.management.MuteManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JoinMuteListener implements JoinListener {

    @Autowired
    private MuteManagementService muteManagementService;

    @Autowired
    private MuteService muteService;

    @Override
    public void execute(Member member, Guild guild, AUserInAServer aUserInAServer) {
        if(muteManagementService.hasActiveMute(aUserInAServer)) {
            log.info("Re-muting user {} which joined the server {}, because the mute has not ended yet.", member.getIdLong(), guild.getIdLong());
            muteService.applyMuteRole(aUserInAServer);
        }
    }

    @Override
    public String getFeature() {
        return ModerationFeatures.MUTING;
    }
}
