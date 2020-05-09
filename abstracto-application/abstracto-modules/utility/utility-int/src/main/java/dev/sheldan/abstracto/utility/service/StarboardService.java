package dev.sheldan.abstracto.utility.service;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.utility.models.database.StarboardPost;
import dev.sheldan.abstracto.utility.models.template.commands.starboard.StarStatsModel;

import java.util.List;

public interface StarboardService {
    void createStarboardPost(CachedMessage message, List<AUserInAServer> userExceptAuthor, AUserInAServer userReacting, AUserInAServer starredUser);
    void updateStarboardPost(StarboardPost post, CachedMessage message, List<AUserInAServer> userExceptAuthor);
    void deleteStarboardMessagePost(StarboardPost message);
    StarStatsModel retrieveStarStats(Long serverId);
}
