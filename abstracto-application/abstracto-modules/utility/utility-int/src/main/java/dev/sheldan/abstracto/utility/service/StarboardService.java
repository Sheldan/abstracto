package dev.sheldan.abstracto.utility.service;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.utility.models.database.StarboardPost;
import dev.sheldan.abstracto.utility.models.template.commands.starboard.StarStatsModel;
import dev.sheldan.abstracto.utility.models.template.commands.starboard.StarStatsPost;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface StarboardService {
    CompletableFuture<Void> createStarboardPost(CachedMessage message, List<AUserInAServer> userExceptAuthor, AUserInAServer userReacting, AUserInAServer starredUser);
    CompletableFuture<Void> updateStarboardPost(StarboardPost post, CachedMessage message, List<AUserInAServer> userExceptAuthor);
    void deleteStarboardMessagePost(StarboardPost message);
    CompletableFuture<StarStatsModel> retrieveStarStats(Long serverId);
    StarStatsPost fromStarboardPost(StarboardPost starboardPost);
}
