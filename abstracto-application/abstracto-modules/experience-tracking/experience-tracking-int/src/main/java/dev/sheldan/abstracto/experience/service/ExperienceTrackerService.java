package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.experience.models.LeaderBoard;
import dev.sheldan.abstracto.experience.models.LeaderBoardEntry;
import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.models.database.AExperienceRole;
import dev.sheldan.abstracto.experience.models.database.AUserExperience;

import java.util.HashMap;
import java.util.List;

public interface ExperienceTrackerService {
    void addExperience(AUserInAServer userInAServer);
    HashMap<Long, List<AServer>> getRuntimeExperience();
    Integer calculateLevel(AUserExperience userInAServer, List<AExperienceLevel> levels);
    AExperienceRole calculateRole(AUserExperience userInAServer, List<AExperienceRole> roles);
    void increaseExpForUser(AUserExperience userInAServer, Long experience, List<AExperienceLevel> levels);
    void handleExperienceGain(List<AServer> serverExp);
    void handleExperienceRoleForUser(AUserExperience userExperience, List<AExperienceRole> roles);
    void syncUserRoles(AServer server);
    void syncUserRolesWithFeedback(AServer server, AChannel channel);
    void syncForSingleUser(AUserExperience userExperience);
    LeaderBoard findLeaderBoardData(AServer server, Integer page);
    LeaderBoardEntry getRankOfUserInServer(AUserInAServer userInAServer);
}
