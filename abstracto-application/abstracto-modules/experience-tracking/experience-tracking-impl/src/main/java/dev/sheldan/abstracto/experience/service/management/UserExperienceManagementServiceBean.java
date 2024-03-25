package dev.sheldan.abstracto.experience.service.management;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.exception.UserInServerNotFoundException;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.experience.model.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.model.database.AExperienceRole;
import dev.sheldan.abstracto.experience.model.database.AUserExperience;
import dev.sheldan.abstracto.experience.model.database.LeaderBoardEntryResult;
import dev.sheldan.abstracto.experience.repository.UserExperienceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
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

    @Override
    public void removeExperienceRoleFromUsers(AExperienceRole experienceRole) {
        repository.removeExperienceRoleFromUsers(experienceRole.getId());
    }

    @Override
    public Optional<AUserExperience> findByUserInServerIdOptional(Long userInServerId) {
       return repository.findById(userInServerId);
    }

    @Override
    public AUserExperience findByUserInServerId(Long userInServerId) {
        return findByUserInServerIdOptional(userInServerId).orElseThrow(() -> new UserInServerNotFoundException(userInServerId));
    }

    @Override
    public AUserExperience createUserInServer(AUserInAServer aUserInAServer) {
        log.info("Creating user experience for user {} in server {}.", aUserInAServer.getUserReference().getId(),aUserInAServer.getServerReference().getId());
        AExperienceLevel startingLevel = experienceLevelManagementService.getLevelOptional(0).orElseThrow(() -> new AbstractoRunTimeException(String.format("Could not find level %s", 0)));
        return AUserExperience
                .builder()
                .experience(0L)
                .messageCount(0L)
                .server(aUserInAServer.getServerReference())
                .experienceGainDisabled(false)
                .levelUpNotification(true)
                .user(aUserInAServer)
                .id(aUserInAServer.getUserInServerId())
                .currentLevel(startingLevel)
                .build();
    }

    @Override
    public List<AUserExperience> loadAllUsers(AServer server) {
        return repository.findByUser_ServerReference(server);
    }

    @Override
    public Page<AUserExperience> loadAllUsersPaginated(AServer server, Pageable pageable) {
        return repository.findAllByServer(server, pageable);
    }

    @Override
    public List<AUserExperience> findLeaderBoardUsersPaginated(AServer aServer, Integer page, Integer size) {
        return repository.findTop10ByUser_ServerReferenceOrderByExperienceDesc(aServer, PageRequest.of(page, size));
    }

    @Override
    public LeaderBoardEntryResult getRankOfUserInServer(AUserExperience userExperience) {
        return repository.getRankOfUserInServer(userExperience.getId(), userExperience.getServer().getId());
    }

    @Override
    public AUserExperience saveUser(AUserExperience userExperience) {
        return repository.save(userExperience);
    }
}


