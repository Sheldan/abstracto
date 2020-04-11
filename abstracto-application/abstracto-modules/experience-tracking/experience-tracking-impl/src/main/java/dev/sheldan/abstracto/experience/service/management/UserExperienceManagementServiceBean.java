package dev.sheldan.abstracto.experience.service.management;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.models.database.AUserExperience;
import dev.sheldan.abstracto.experience.repository.UserExperienceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
public class UserExperienceManagementServiceBean implements UserExperienceManagementService {

    @Autowired
    private UserExperienceRepository repository;

    @Autowired
    private ExperienceLevelManagementService experienceLevelManagementService;

    @Override
    public void setExperienceTo(AUserExperience aUserInAServer, Long experience) {
        aUserInAServer.setExperience(experience);
        repository.save(aUserInAServer);
    }

    @Override
    public AUserExperience findUserInServer(AUserInAServer aUserInAServer) {
        Optional<AUserExperience> byId = repository.findById(aUserInAServer.getUserInServerId());
        return byId.orElseGet(() -> createUserInServer(aUserInAServer));
    }

    @Override
    public AUserExperience createUserInServer(AUserInAServer aUserInAServer) {
        AExperienceLevel startingLevel = experienceLevelManagementService.getLevel(0);
        AUserExperience userExperience = AUserExperience
                .builder()
                .experience(0L)
                .user(aUserInAServer)
                .id(aUserInAServer.getUserInServerId())
                .currentLevel(startingLevel)
                .build();
        repository.save(userExperience);
        return userExperience;
    }

    @Override
    public void saveUser(AUserExperience userExperience) {
        repository.save(userExperience);
    }
}


