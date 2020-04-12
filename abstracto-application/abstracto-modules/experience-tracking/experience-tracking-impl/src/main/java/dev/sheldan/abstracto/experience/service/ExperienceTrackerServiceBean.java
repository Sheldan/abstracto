package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.experience.LeaderBoardEntryResult;
import dev.sheldan.abstracto.experience.models.LeaderBoard;
import dev.sheldan.abstracto.experience.models.LeaderBoardEntry;
import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.models.database.AExperienceRole;
import dev.sheldan.abstracto.experience.models.database.AUserExperience;
import dev.sheldan.abstracto.experience.models.templates.UserSyncStatusModel;
import dev.sheldan.abstracto.experience.service.management.ExperienceLevelManagementService;
import dev.sheldan.abstracto.experience.service.management.ExperienceRoleManagementService;
import dev.sheldan.abstracto.experience.service.management.UserExperienceManagementService;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;

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

    @Autowired
    private MessageService messageService;

    @Autowired
    private TemplateService templateService;

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
                return lastRole;
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
    }

    @Transactional
    @Override
    public void handleExperienceGain(List<AServer> servers) {
        servers.forEach(serverExp -> {
            log.debug("Handling experience for server {}", serverExp.getId());
            int minExp = configService.getDoubleValue("minExp", serverExp.getId()).intValue();
            int maxExp = configService.getDoubleValue("maxExp", serverExp.getId()).intValue();
            Integer multiplier = configService.getDoubleValue("expMultiplier", serverExp.getId()).intValue();
            PrimitiveIterator.OfInt iterator = new Random().ints(serverExp.getUsers().size(), minExp, maxExp + 1).iterator();
            List<AExperienceLevel> levels = experienceLevelManagementService.getLevelConfig();
            List<AExperienceRole> roles = experienceRoleManagementService.getExperienceRoleForServer(serverExp);
            levels.sort(Comparator.comparing(AExperienceLevel::getExperienceNeeded));
            serverExp.getUsers().forEach(userInAServer -> {
                Integer gainedExperience = iterator.next();
                gainedExperience *= multiplier;
                log.debug("Handling {}. The user gains {}", userInAServer.getUserReference().getId(), gainedExperience);
                AUserExperience userExperience = userExperienceManagementService.findUserInServer(userInAServer);
                increaseExpForUser(userExperience, gainedExperience.longValue(), levels);
                userExperience.setMessageCount(userExperience.getMessageCount() + 1);
                handleExperienceRoleForUser(userExperience, roles);
            });
        });
    }

    @Override
    public void handleExperienceRoleForUser(AUserExperience userExperience, List<AExperienceRole> roles) {
        AExperienceRole role = calculateRole(userExperience, roles);
        boolean currentlyHasNoExperienceRole = userExperience.getCurrentExperienceRole() == null;
        if(role == null) {
            if(!currentlyHasNoExperienceRole){
                roleService.removeRoleFromUser(userExperience.getUser(), userExperience.getCurrentExperienceRole().getRole());
            }
            return;
        }
        if(currentlyHasNoExperienceRole || !role.getRole().getId().equals(userExperience.getCurrentExperienceRole().getRole().getId())) {
            log.info("User {} gets a new role {}", userExperience.getUser().getUserReference().getId(), role.getRole().getId());
            if(!currentlyHasNoExperienceRole) {
                roleService.removeRoleFromUser(userExperience.getUser(), userExperience.getCurrentExperienceRole().getRole());
            }
            userExperience.setCurrentExperienceRole(role);
            roleService.addRoleToUser(userExperience.getUser(), userExperience.getCurrentExperienceRole().getRole());
        }
    }

    @Override
    public void syncUserRoles(AServer server) {
        List<AUserExperience> aUserExperiences = userExperienceManagementService.loadAllUsers(server);
        log.info("Found {} users to synchronize", aUserExperiences.size());
        List<AExperienceRole> roles = experienceRoleManagementService.getExperienceRoleForServer(server);
        for (int i = 0; i < aUserExperiences.size(); i++) {
            AUserExperience userExperience = aUserExperiences.get(i);
            log.debug("Synchronizing {} out of {}", i, aUserExperiences.size());
            handleExperienceRoleForUser(userExperience, roles);
        }
    }

    @Override
    public void syncUserRolesWithFeedback(AServer server, AChannel channel) {
        List<AUserExperience> aUserExperiences = userExperienceManagementService.loadAllUsers(server);
        log.info("Found {} users to synchronize", aUserExperiences.size());
        List<AExperienceRole> roles = experienceRoleManagementService.getExperienceRoleForServer(server);
        UserSyncStatusModel statusModel = UserSyncStatusModel.builder().currentCount(0).totalUserCount(aUserExperiences.size()).build();
        MessageToSend status = templateService.renderEmbedTemplate("status_message", statusModel);
        try {
            Message statusMessage = messageService.createStatusMessage(status, channel).get();
            for (int i = 0; i < aUserExperiences.size(); i++) {
                if((i % 100) == 1) {
                    UserSyncStatusModel incrementalStatusModel = UserSyncStatusModel.builder().currentCount(i).totalUserCount(aUserExperiences.size()).build();
                    status = templateService.renderEmbedTemplate("status_message", incrementalStatusModel);
                    messageService.updateStatusMessage(channel, statusMessage.getIdLong(), status);
                }
                log.debug("Synchronizing {} out of {}", i, aUserExperiences.size());
                AUserExperience userExperience = aUserExperiences.get(i);
                handleExperienceRoleForUser(userExperience, roles);
            }
            UserSyncStatusModel incrementalStatusModel = UserSyncStatusModel.builder().currentCount(aUserExperiences.size()).totalUserCount(aUserExperiences.size()).build();
            status = templateService.renderEmbedTemplate("status_message", incrementalStatusModel);
            messageService.updateStatusMessage(channel, statusMessage.getIdLong(), status);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void syncForSingleUser(AUserExperience userExperience) {
        log.info("Synchronizing for user {}", userExperience.getUser().getUserReference().getId());
        List<AExperienceRole> roles = experienceRoleManagementService.getExperienceRoleForServer(userExperience.getUser().getServerReference());
        handleExperienceRoleForUser(userExperience, roles);
    }

    @Override
    public LeaderBoard findLeaderBoardData(AServer server, Integer page) {
        List<AUserExperience> experiences = userExperienceManagementService.findLeaderboardUsersPaginated(server, page * 10, (page +1) * 10);
        log.info("We found {}", experiences.size());
        List<LeaderBoardEntry> entries = new ArrayList<>();
        for (int i = 0; i < experiences.size(); i++) {
            AUserExperience userExperience = experiences.get(i);
            entries.add(LeaderBoardEntry.builder().experience(userExperience).rank(i + 1).build());
        }
        return LeaderBoard.builder().entries(entries).build();
    }

    @Override
    public LeaderBoardEntry getRankOfUserInServer(AUserInAServer userInAServer) {
        AUserExperience experience = userExperienceManagementService.findUserInServer(userInAServer);
        LeaderBoardEntryResult rankOfUserInServer = userExperienceManagementService.getRankOfUserInServer(experience);
        AUserExperience aUserExperience = AUserExperience
                .builder()
                .experience(rankOfUserInServer.getExperience())
                .user(userInAServer)
                .messageCount(rankOfUserInServer.getMessageCount())
                .id(userInAServer.getUserInServerId())
                .currentLevel(experienceLevelManagementService.getLevel(rankOfUserInServer.getLevel()))
                .build();
        return LeaderBoardEntry.builder().experience(aUserExperience).rank(rankOfUserInServer.getRank()).build();
    }

}
