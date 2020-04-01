package dev.sheldan.abstracto.utility.service;

import dev.sheldan.abstracto.core.models.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.utility.models.StarboardPost;
import dev.sheldan.abstracto.utility.models.template.starboard.StarStatsModel;

import java.util.List;

public interface StarboardService {
    void createStarboardPost(CachedMessage message, List<AUser> userExceptAuthor, AUserInAServer userReacting, AUserInAServer starredUser);
    void updateStarboardPost(StarboardPost post, CachedMessage message, List<AUser> userExceptAuthor);
    void removeStarboardPost(StarboardPost message);
    StarStatsModel retrieveStarStats(Long serverId);
}
