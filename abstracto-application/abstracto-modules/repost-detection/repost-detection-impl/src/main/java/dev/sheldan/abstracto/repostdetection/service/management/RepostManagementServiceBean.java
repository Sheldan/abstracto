package dev.sheldan.abstracto.repostdetection.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.repostdetection.exception.RepostNotFoundException;
import dev.sheldan.abstracto.repostdetection.model.database.PostedImage;
import dev.sheldan.abstracto.repostdetection.model.database.Repost;
import dev.sheldan.abstracto.repostdetection.model.database.embed.RepostIdentifier;
import dev.sheldan.abstracto.repostdetection.model.database.result.RepostLeaderboardResult;
import dev.sheldan.abstracto.repostdetection.repository.RepostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class RepostManagementServiceBean implements RepostManagementService {

    @Autowired
    private RepostRepository repostRepository;

    @Override
    public Repost createRepost(PostedImage postedImage, AUserInAServer poster) {
        Repost repost = Repost
                .builder()
                .originalPost(postedImage)
                .poster(poster)
                .repostId(buildRepostIdentifier(postedImage, poster))
                .server(postedImage.getServer())
                .count(1)
                .build();

        repostRepository.save(repost);
        return repost;
    }

    @Override
    public Repost setRepostCount(PostedImage postedImage, AUserInAServer poster, Integer newCount) {
        Repost repost = findRepost(postedImage, poster);
        repost.setCount(newCount);
        return repost;
    }

    @Override
    public Repost findRepost(PostedImage postedImage, AUserInAServer poster) {
        return findRepostOptional(postedImage, poster)
                .orElseThrow(() -> new RepostNotFoundException(postedImage.getPostId().getMessageId(), postedImage.getPostId().getPosition(), poster.getUserInServerId()));
    }

    @Override
    public Optional<Repost> findRepostOptional(PostedImage postedImage, AUserInAServer poster) {
        return repostRepository.findById(buildRepostIdentifier(postedImage, poster));
    }

    @Override
    public List<RepostLeaderboardResult> findTopRepostingUsersOfServer(AServer server, Integer page, Integer pageSize) {
        return findTopRepostingUsersOfServer(server.getId(), page, pageSize);
    }

    @Override
    public List<RepostLeaderboardResult> findTopRepostingUsersOfServer(Long serverId, Integer page, Integer pageSize) {
        return repostRepository.findTopRepostingUsers(serverId, PageRequest.of(page - 1, pageSize));
    }

    @Override
    public RepostLeaderboardResult getRepostRankOfUser(AUserInAServer aUserInAServer) {
        return repostRepository.getRepostRankOfUserInServer(aUserInAServer.getUserInServerId(), aUserInAServer.getServerReference().getId());
    }

    @Override
    public void deleteRepostsFromUser(AUserInAServer aUserInAServer) {
        repostRepository.deleteByRepostId_UserInServerIdAndServerId(aUserInAServer.getUserInServerId(), aUserInAServer.getServerReference().getId());
    }

    @Override
    public void deleteRepostsFromServer(AServer server) {
        repostRepository.deleteByServerId(server.getId());
    }

    private RepostIdentifier buildRepostIdentifier(PostedImage postedImage, AUserInAServer poster) {
        return new RepostIdentifier(postedImage.getPostId().getMessageId(), postedImage.getPostId().getPosition(), poster.getUserInServerId());
    }
}
