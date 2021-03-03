package dev.sheldan.abstracto.experience.listener;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncJoinListener;
import dev.sheldan.abstracto.core.listener.sync.jda.JoinListener;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.experience.config.features.ExperienceFeature;
import dev.sheldan.abstracto.experience.models.database.AUserExperience;
import dev.sheldan.abstracto.experience.service.AUserExperienceService;
import dev.sheldan.abstracto.experience.service.management.UserExperienceManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * If a {@link Member member} joins, this {@link JoinListener listener} retrieves the previously stored {@link AUserExperience experience} and gives the
 * member the necessary {@link net.dv8tion.jda.api.entities.Role role} according to the current configuration, if any
 */
@Component
@Slf4j
public class JoiningUserRoleListener implements AsyncJoinListener {

    @Autowired
    private UserExperienceManagementService userExperienceManagementService;

    @Autowired
    private AUserExperienceService userExperienceService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Override
    public void execute(ServerUser serverUser) {
        AUserInAServer userInAServer = userInServerManagementService.loadOrCreateUser(serverUser.getServerId(), serverUser.getUserId());
        AUserExperience userExperience = userExperienceManagementService.findUserInServer(userInAServer);
        Long userInServerId = userInAServer.getUserInServerId();
        if(userExperience != null) {
            log.info("User {} joined {} with previous experience. Setting up experience role again (if necessary).", serverUser.getUserId(), serverUser.getServerId());
            userExperienceService.syncForSingleUser(userExperience).thenAccept(result ->
                log.info("Finished re-assigning experience for re-joining user {} in server {}.", userInServerId, serverUser.getServerId())
            );
        } else {
            log.info("Joined user {} in server {} does not have any previous experience. Not setting up anything.", serverUser.getUserId(), serverUser.getServerId());
        }
    }

    @Override
    public FeatureEnum getFeature() {
        return ExperienceFeature.EXPERIENCE;
    }

}
