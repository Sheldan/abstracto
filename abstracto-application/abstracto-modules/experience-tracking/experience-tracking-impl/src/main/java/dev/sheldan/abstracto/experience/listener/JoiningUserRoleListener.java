package dev.sheldan.abstracto.experience.listener;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.listener.JoinListener;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.experience.config.features.ExperienceFeature;
import dev.sheldan.abstracto.experience.models.database.AUserExperience;
import dev.sheldan.abstracto.experience.service.AUserExperienceService;
import dev.sheldan.abstracto.experience.service.management.UserExperienceManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * If a user joins, this {@link JoinListener} retrieves the previously stored {@link AUserExperience} and gives the
 * {@link Member} the necessary {@link net.dv8tion.jda.api.entities.Role} according to the current configuration
 */
@Component
@Slf4j
public class JoiningUserRoleListener implements JoinListener {

    @Autowired
    private UserExperienceManagementService userExperienceManagementService;

    @Autowired
    private AUserExperienceService userExperienceService;

    @Override
    public void execute(Member member, Guild guild, AUserInAServer aUserInAServer) {
        AUserExperience userExperience = userExperienceManagementService.findUserInServer(aUserInAServer);
        Long userInServerId = aUserInAServer.getUserInServerId();
        if(userExperience != null) {
            log.info("User {} joined {} with previous experience. Setting up experience role again (if necessary).", member.getUser().getIdLong(), guild.getIdLong());
            userExperienceService.syncForSingleUser(userExperience).thenAccept(result ->
                log.info("Finished re-assigning experience for re-joning user {} in server {}.", userInServerId, guild.getIdLong())
            );
        } else {
            log.info("Joined user {} in server {} does not have any previous experience. Not setting up anything.", member.getId(), guild.getId());
        }
    }

    @Override
    public FeatureEnum getFeature() {
        return ExperienceFeature.EXPERIENCE;
    }
}
