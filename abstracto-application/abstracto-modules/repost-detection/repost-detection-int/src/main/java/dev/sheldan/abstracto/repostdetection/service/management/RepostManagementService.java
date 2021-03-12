package dev.sheldan.abstracto.repostdetection.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.repostdetection.model.database.PostedImage;
import dev.sheldan.abstracto.repostdetection.model.database.Repost;
import dev.sheldan.abstracto.repostdetection.model.database.result.RepostLeaderboardResult;

import java.util.List;
import java.util.Optional;

public interface RepostManagementService {
    Repost createRepost(PostedImage postedImage, AUserInAServer poster);
    Repost setRepostCount(PostedImage postedImage, AUserInAServer poster, Integer newCount);
    Repost findRepost(PostedImage postedImage, AUserInAServer poster);
    Optional<Repost> findRepostOptional(PostedImage postedImage, AUserInAServer poster);
    List<RepostLeaderboardResult> findTopRepostingUsersOfServer(AServer server, Integer page, Integer pageSize);
    List<RepostLeaderboardResult> findTopRepostingUsersOfServer(Long serverId, Integer page, Integer pageSize);
    RepostLeaderboardResult getRepostRankOfUser(AUserInAServer aUserInAServer);
    void deleteRepostsFromUser(AUserInAServer aUserInAServer);
    void deleteRepostsFromServer(AServer server);
}
