package dev.sheldan.abstracto.utility.service.management;

import dev.sheldan.abstracto.core.models.AServerAChannelAUser;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.utility.exception.PostedImageNotFoundException;
import dev.sheldan.abstracto.utility.models.database.PostedImage;
import dev.sheldan.abstracto.utility.models.database.embed.PostIdentifier;
import dev.sheldan.abstracto.utility.repository.PostedImageRepository;
import dev.sheldan.abstracto.utility.service.RepostServiceBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PostedImageManagementBean implements PostedImageManagement {

    @Autowired
    private PostedImageRepository postedImageRepository;

    @Autowired
    private RepostCheckChannelGroupManagement checkChannelBean;

    @Override
    public PostedImage createPost(AServerAChannelAUser creation, Long messageId, String hash, Integer index) {
        PostedImage post = PostedImage
                .builder()
                .imageHash(hash)
                .postId(new PostIdentifier(messageId, index))
                .poster(creation.getAUserInAServer())
                .server(creation.getGuild())
                .postedChannel(creation.getChannel())
                .build();

        postedImageRepository.save(post);
        return post;
    }

    @Override
    public boolean postWitHashExists(String hash, AServer server) {
        return postedImageRepository.existsByImageHashAndServerId(hash, server.getId());
    }

    @Override
    public Optional<PostedImage> getPostWithHash(String hash, AServer server) {
        return postedImageRepository.findByImageHashAndServerId(hash, server.getId());
    }

    @Override
    public boolean messageHasBeenCovered(Long messageId) {
        return postedImageRepository.existsByPostId_MessageId(messageId);
    }

    @Override
    public boolean messageEmbedsHaveBeenCovered(Long messageId) {
        return postedImageRepository.existsByPostId_MessageIdAndPostId_PositionGreaterThan(messageId, RepostServiceBean.EMBEDDED_LINK_POSITION_START_INDEX - 1);
    }

    @Override
    public List<PostedImage> getAllFromMessage(Long messageId) {
        return postedImageRepository.findByPostId_MessageId(messageId);
    }

    @Override
    public Optional<PostedImage> getPostFromMessageAndPositionOptional(Long messageId, Integer position) {
        return postedImageRepository.findById(new PostIdentifier(messageId, position));
    }

    @Override
    public PostedImage getPostFromMessageAndPosition(Long messageId, Integer position) {
        return getPostFromMessageAndPositionOptional(messageId, position).orElseThrow(() -> new PostedImageNotFoundException(messageId, position));
    }

    @Override
    public void removePostedImagesOf(AUserInAServer poster) {
        postedImageRepository.deleteByPoster(poster);
    }

    @Override
    public void removedPostedImagesIn(AServer aServer) {
        postedImageRepository.deleteByServer(aServer);
    }
}
