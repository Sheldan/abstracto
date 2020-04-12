package dev.sheldan.abstracto.experience.service.management;


import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.experience.LeaderBoardEntryResult;
import dev.sheldan.abstracto.experience.models.LeaderBoardEntry;
import dev.sheldan.abstracto.experience.models.database.AUserExperience;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserExperienceManagementService {
    void setExperienceTo(AUserExperience aUserInAServer, Long experience);
    AUserExperience findUserInServer(AUserInAServer aUserInAServer);
    AUserExperience createUserInServer(AUserInAServer aUserInAServer);
    void saveUser(AUserExperience userExperience);
    List<AUserExperience> loadAllUsers(AServer server);
    List<AUserExperience> findLeaderboardUsersPaginated(AServer server, Integer start, Integer end);
    LeaderBoardEntryResult getRankOfUserInServer(AUserExperience userExperience);
}
