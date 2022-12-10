package dev.sheldan.abstracto.experience.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncUpdatePendingListener;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.listener.MemberUpdatePendingModel;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.experience.config.ExperienceFeatureDefinition;
import dev.sheldan.abstracto.experience.model.database.AUserExperience;
import dev.sheldan.abstracto.experience.service.AUserExperienceService;
import dev.sheldan.abstracto.experience.service.management.UserExperienceManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * If a {@link Member member} updates the pending status, this {@link AsyncUpdatePendingListener listener} retrieves the previously stored {@link AUserExperience experience} and gives the
 * member the necessary {@link net.dv8tion.jda.api.entities.Role role} according to the current configuration, if any
 */
@Component
@Slf4j
public class MemberPendingRoleListener implements AsyncUpdatePendingListener {

    @Autowired
    private UserExperienceManagementService userExperienceManagementService;

    @Autowired
    private AUserExperienceService userExperienceService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Override
    public DefaultListenerResult execute(MemberUpdatePendingModel model) {
        Optional<AUserInAServer> userInAServerOptional = userInServerManagementService.loadUserOptional(model.getServerId(), model.getUser().getUserId());
        userInAServerOptional.ifPresent(aUserInAServer -> {
            Optional<AUserExperience> userExperienceOptional = userExperienceManagementService.findByUserInServerIdOptional(aUserInAServer.getUserInServerId());
            if(userExperienceOptional.isPresent()) {
                log.info("User {} updated pending status {} with previous experience. Setting up experience role again (if necessary).", model.getUser().getUserId(), model.getServerId());
                userExperienceService.syncForSingleUser(userExperienceOptional.get(), model.getMember()).thenAccept(result ->
                        log.info("Finished re-assigning experience for update pending user {} in server {}.", model.getUser().getUserId(), model.getServerId())
                );
            } else {
                log.info("Member updating pending {} in server {} does not have any previous experience. Not setting up anything.", model.getUser().getUserId(), model.getServerId());
            }
        });

        return DefaultListenerResult.PROCESSED;
    }

    @Override
    public FeatureDefinition getFeature() {
        return ExperienceFeatureDefinition.EXPERIENCE;
    }

}
