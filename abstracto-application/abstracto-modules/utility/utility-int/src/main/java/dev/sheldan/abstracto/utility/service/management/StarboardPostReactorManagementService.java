package dev.sheldan.abstracto.utility.service.management;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.utility.models.database.StarboardPost;
import dev.sheldan.abstracto.utility.models.database.StarboardPostReaction;
import dev.sheldan.abstracto.utility.models.template.commands.starboard.StarStatsUser;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface StarboardPostReactorManagementService {
    StarboardPostReaction addReactor(StarboardPost post, AUserInAServer user);
    void removeReactor(StarboardPost post, AUserInAServer user);
    void removeReactors(StarboardPost post);
    Integer getStarCount(Long serverId);
    List<CompletableFuture<StarStatsUser>> retrieveTopStarGiver(Long serverId, Integer count);
    List<CompletableFuture<StarStatsUser>> retrieveTopStarReceiver(Long serverId, Integer count);
}
