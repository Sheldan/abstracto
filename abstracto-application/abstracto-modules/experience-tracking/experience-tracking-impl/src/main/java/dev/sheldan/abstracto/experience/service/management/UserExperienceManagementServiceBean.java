package dev.sheldan.abstracto.experience.service.management;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.experience.models.database.LeaderBoardEntryResult;
import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.models.database.AUserExperience;
import dev.sheldan.abstracto.experience.repository.UserExperienceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;


@Component
public class UserExperienceManagementServiceBean implements UserExperienceManagementService {

    @Autowired
    private UserExperienceRepository repository;

    @Autowired
    private ExperienceLevelManagementService experienceLevelManagementService;

    @Override
    public AUserExperience findUserInServer(AUserInAServer aUserInAServer) {
        Optional<AUserExperience> byId = repository.findById(aUserInAServer.getUserInServerId());
        return byId.orElseGet(() -> createUserInServer(aUserInAServer));
    }

    /**
     * Initializes the {@link AUserExperience} with default values the following: 0 experience, 0 messages and experience gain enabled
     * @param aUserInAServer The {@link AUserInAServer} to create the {@link AUserExperience} object for.
     * @return The created/changed {@link AUserExperience} object
     */
    @Override
    public AUserExperience createUserInServer(AUserInAServer aUserInAServer) {
        AExperienceLevel startingLevel = experienceLevelManagementService.getLevel(0).orElseThrow(() -> new AbstractoRunTimeException(String.format("Could not find level %s", 0)));
        return AUserExperience
                .builder()
                .experience(0L)
                .messageCount(0L)
                .experienceGainDisabled(false)
                .user(aUserInAServer)
                .id(aUserInAServer.getUserInServerId())
                .currentLevel(startingLevel)
                .build();
    }

    @Override
    public List<AUserExperience> loadAllUsers(AServer server) {
        return repository.findByUser_ServerReference(server);
    }

    /**
     * Creates or updates the {@link AUserExperience} object. Does not change the level or the role.
     * @param user The {@link AUserInAServer} to increase the experience for
     * @param experience The experience amount to increase by
     * @param messageCount The amount of messags to increase the count by
     * @return The created/changed {@link AUserExperience} object
     */
    @Override
    public AUserExperience incrementExpForUser(AUserInAServer user, Long experience, Long messageCount) {
        Optional<AUserExperience> byId = repository.findById(user.getUserInServerId());
        if(byId.isPresent()) {
            AUserExperience userExperience = byId.get();
            if(Boolean.FALSE.equals(userExperience.getExperienceGainDisabled())) {
                userExperience.setMessageCount(userExperience.getMessageCount() + messageCount);
                userExperience.setExperience(userExperience.getExperience() + experience);
            }
            return userExperience;
        } else {
            AExperienceLevel startingLevel = experienceLevelManagementService.getLevel(0).orElseThrow(() -> new AbstractoRunTimeException(String.format("Could not find level %s", 0)));
            return AUserExperience
                    .builder()
                    .experience(experience)
                    .messageCount(messageCount)
                    .experienceGainDisabled(false)
                    .user(user)
                    .id(user.getUserInServerId())
                    .currentLevel(startingLevel)
                    .build();
        }
    }

    @Override
    public List<AUserExperience> findLeaderBoardUsersPaginated(AServer aServer, Integer start, Integer end) {
        return repository.findTop10ByUser_ServerReferenceOrderByExperienceDesc(aServer, PageRequest.of(start, end));
    }

    @Override
    public LeaderBoardEntryResult getRankOfUserInServer(AUserExperience userExperience) {
        return repository.getRankOfUserInServer(userExperience.getId(), userExperience.getUser().getServerReference().getId());
    }

    @Override
    public AUserExperience saveUser(AUserExperience userExperience) {
        return repository.save(userExperience);
    }
}


