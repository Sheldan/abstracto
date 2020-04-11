package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.models.database.AExperienceRole;
import dev.sheldan.abstracto.experience.models.database.AUserExperience;
import dev.sheldan.abstracto.experience.service.management.ExperienceLevelManagementService;
import dev.sheldan.abstracto.experience.service.management.ExperienceRoleManagementService;
import dev.sheldan.abstracto.experience.service.management.UserExperienceManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Component
@Slf4j
public class ExperienceTrackerServiceBean implements ExperienceTrackerService {

    private HashMap<Long, List<AServer>> runtimeExperience = new HashMap<>();

    @Autowired
    private UserExperienceManagementService userExperienceManagementService;

    @Autowired
    private ExperienceLevelService experienceLevelService;

    @Autowired
    private ExperienceLevelManagementService experienceLevelManagementService;

    @Autowired
    private ExperienceRoleManagementService experienceRoleManagementService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private RoleService roleService;

    @Override
    public void addExperience(AUserInAServer userInAServer) {
        Long second = Instant.now().getEpochSecond() / 60;
        if(runtimeExperience.containsKey(second)) {
            List<AServer> existing = runtimeExperience.get(second);
            existing.forEach(server -> {
                if(server.getUsers().stream().noneMatch(userInAServer1 -> userInAServer.getUserInServerId().equals(userInAServer1.getUserInServerId()))) {
                    server.getUsers().add(userInAServer);
                }
            });

        } else {
            AServer server = AServer
                    .builder()
                    .id(userInAServer.getServerReference().getId())
                    .build();
            server.getUsers().add(userInAServer);
            runtimeExperience.put(second, Arrays.asList(server));
        }
    }

    @Override
    public HashMap<Long, List<AServer>> getRuntimeExperience() {
        return runtimeExperience;
    }

    @Override
    public Integer calculateLevel(AUserExperience userInAServer, List<AExperienceLevel> levels) {
        AExperienceLevel lastLevel = levels.get(0);
        for (AExperienceLevel level : levels) {
            if(level.getExperienceNeeded() >= userInAServer.getExperience()) {
                return lastLevel.getLevel();
            } else {
                lastLevel = level;
            }
        }
        return lastLevel.getLevel();
    }

    @Override
    public AExperienceRole calculateRole(AUserExperience userInAServer, List<AExperienceRole> roles) {
        if(roles.size() == 0) {
            return null;
        }
        AExperienceRole lastRole = null;
        for (AExperienceRole experienceRole : roles) {
            if(userInAServer.getCurrentLevel().getLevel() >= experienceRole.getLevel().getLevel()) {
                lastRole = experienceRole;
            } else {
                return experienceRole;
            }
        }
        return lastRole;
    }

    @Override
    public void increaseExpForUser(AUserExperience userInAServer, Long experience, List<AExperienceLevel> levels) {
        userInAServer.setExperience(userInAServer.getExperience() + experience);
        Integer correctLevel = calculateLevel(userInAServer, levels);
        Integer currentLevel = userInAServer.getCurrentLevel() != null ? userInAServer.getCurrentLevel().getLevel() : 0;
        if(!correctLevel.equals(currentLevel)) {
            log.info("User {} leveled from {} to {}", userInAServer.getUser().getUserReference().getId(), currentLevel, correctLevel);
            userInAServer.setCurrentLevel(experienceLevelManagementService.getLevel(correctLevel));
        }
        userExperienceManagementService.saveUser(userInAServer);
    }

    @Transactional
    @Override
    public void handleExperienceGain(List<AServer> servers) {
        servers.forEach(serverExp -> {
            log.info("Handling experience for server {}", serverExp.getId());
            int minExp = configService.getDoubleValue("minExp", serverExp.getId()).intValue();
            int maxExp = configService.getDoubleValue("maxExp", serverExp.getId()).intValue();
            Integer multiplier = configService.getDoubleValue("multiplier", serverExp.getId()).intValue();
            PrimitiveIterator.OfInt iterator = new Random().ints(serverExp.getUsers().size(), minExp, maxExp + 1).iterator();
            List<AExperienceLevel> levels = experienceLevelManagementService.getLevelConfig();
            List<AExperienceRole> roles = experienceRoleManagementService.getExperienceRoleForServer(serverExp);
            levels.sort(Comparator.comparing(AExperienceLevel::getExperienceNeeded));
            serverExp.getUsers().forEach(userInAServer -> {
                Integer gainedExperience = iterator.next();
                gainedExperience *= multiplier;
                log.info("Handling {}. The user gains {}", userInAServer.getUserReference().getId(), gainedExperience);
                AUserExperience userExperience = userExperienceManagementService.findUserInServer(userInAServer);
                increaseExpForUser(userExperience, gainedExperience.longValue(), levels);
                handleExperienceRoleForUser(userExperience, roles);
            });
        });
    }

    @Override
    public void handleExperienceRoleForUser(AUserExperience userExperience, List<AExperienceRole> roles) {
        AExperienceRole role = calculateRole(userExperience, roles);
        if(role == null) {
            return;
        }
        boolean currentlyHasNoExperienceRole = userExperience.getCurrentExperienceRole() == null;
        if(currentlyHasNoExperienceRole || !role.getRole().getId().equals(userExperience.getCurrentExperienceRole().getId())) {
            log.info("User {} gets a new role {}", userExperience.getUser().getUserReference().getId(), role.getRole().getId());
            if(!currentlyHasNoExperienceRole) {
                roleService.removeRoleFromUser(userExperience.getUser(), userExperience.getCurrentExperienceRole().getRole());
            }
            userExperience.setCurrentExperienceRole(role);
            roleService.addRoleToUser(userExperience.getUser(), userExperience.getCurrentExperienceRole().getRole());
        }
    }

}
