package dev.sheldan.abstracto.experience.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncJoinListener;
import dev.sheldan.abstracto.core.listener.sync.jda.JoinListener;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.listener.MemberJoinModel;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.experience.config.ExperienceFeatureDefinition;
import dev.sheldan.abstracto.experience.config.ExperienceFeatureMode;
import dev.sheldan.abstracto.experience.model.database.AUserExperience;
import dev.sheldan.abstracto.experience.service.AUserExperienceService;
import dev.sheldan.abstracto.experience.service.LevelActionService;
import dev.sheldan.abstracto.experience.service.management.UserExperienceManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

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

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private LevelActionService levelActionService;

    @Override
    public DefaultListenerResult execute(MemberJoinModel model) {
        if(model.getMember().isPending()) {
            log.info("Joining member {} in guild {} is still pending - ignoring for experience role assignment.", model.getJoiningUser().getUserId(), model.getJoiningUser().getServerId());
            return DefaultListenerResult.IGNORED;
        }
        Optional<AUserInAServer> userInAServerOptional = userInServerManagementService.loadUserOptional(model.getServerId(), model.getJoiningUser().getUserId());
        userInAServerOptional.ifPresent(aUserInAServer -> {
            Long userInServerId = aUserInAServer.getUserInServerId();
            Optional<AUserExperience> userExperienceOptional = userExperienceManagementService.findByUserInServerIdOptional(userInServerId);
            if(userExperienceOptional.isPresent()) {
                log.info("User {} joined {} with previous experience. Setting up experience role again (if necessary).", model.getJoiningUser().getUserId(), model.getServerId());
                AUserExperience aUserExperience = userExperienceOptional.get();
                userExperienceService.syncForSingleUser(aUserExperience, model.getMember(), true).thenAccept(result ->
                        log.info("Finished re-assigning experience for re-joining user {} in server {}.", model.getJoiningUser().getUserId(), model.getServerId())
                );
                if(featureModeService.featureModeActive(ExperienceFeatureDefinition.EXPERIENCE, aUserInAServer.getServerReference() , ExperienceFeatureMode.LEVEL_ACTION)) {
                    levelActionService.applyLevelActionsToUser(aUserExperience)
                            .thenAccept(unused -> {
                                log.info("Executed level actions for user {}.", userInServerId);
                            })
                            .exceptionally(throwable -> {
                                log.warn("Failed to execute level actions for user {}.", userInServerId, throwable);
                                return null;
                            });
                }
            } else {
                log.info("Joined user {} in server {} does not have any previous experience. Not setting up anything.", model.getJoiningUser().getUserId(), model.getServerId());
            }
        });

        return DefaultListenerResult.PROCESSED;
    }

    @Override
    public FeatureDefinition getFeature() {
        return ExperienceFeatureDefinition.EXPERIENCE;
    }

}
