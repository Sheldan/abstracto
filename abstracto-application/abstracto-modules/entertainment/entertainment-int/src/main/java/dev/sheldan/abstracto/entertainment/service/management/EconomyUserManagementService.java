package dev.sheldan.abstracto.entertainment.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.entertainment.model.database.EconomyLeaderboardResult;
import dev.sheldan.abstracto.entertainment.model.database.EconomyUser;

import java.util.List;
import java.util.Optional;

public interface EconomyUserManagementService {
    EconomyUser createUser(AUserInAServer aUserInAServer);
    Optional<EconomyUser> getUser(AUserInAServer aUserInAServer);
    EconomyLeaderboardResult getRankOfUserInServer(AUserInAServer aUserInAServer);
    List<EconomyUser> getRanksInServer(AServer server, Integer page, Integer size);
}
