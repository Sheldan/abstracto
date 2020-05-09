package dev.sheldan.abstracto.utility.service.management;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.utility.models.database.StarboardPost;
import dev.sheldan.abstracto.utility.models.template.commands.starboard.StarStatsUser;

import java.util.List;

public interface StarboardPostReactorManagementService {
    void addReactor(StarboardPost post, AUserInAServer user);
    void removeReactor(StarboardPost post, AUserInAServer user);
    void removeReactors(StarboardPost post);
    Integer getStarCount(Long serverId);
    List<StarStatsUser> retrieveTopStarGiver(Long serverId, Integer count);
    List<StarStatsUser> retrieveTopStarReceiver(Long serverId, Integer count);
}
