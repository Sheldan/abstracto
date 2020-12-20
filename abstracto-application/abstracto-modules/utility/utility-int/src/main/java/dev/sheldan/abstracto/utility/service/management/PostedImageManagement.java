package dev.sheldan.abstracto.utility.service.management;

import dev.sheldan.abstracto.core.models.AServerAChannelAUser;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.utility.models.database.PostedImage;

import java.util.List;
import java.util.Optional;

public interface PostedImageManagement {
    PostedImage createPost(AServerAChannelAUser creation, Long messageId, String hash, Integer index);
    boolean postWitHashExists(String hash, AServer server);
    Optional<PostedImage> getPostWithHash(String hash, AServer server);
    boolean messageHasBeenCovered(Long messageId);
    boolean messageEmbedsHaveBeenCovered(Long messageId);
    List<PostedImage> getAllFromMessage(Long messageId);
    Optional<PostedImage> getPostFromMessageAndPositionOptional(Long messageId, Integer position);
    PostedImage getPostFromMessageAndPosition(Long messageId, Integer position);
    void removePostedImagesOf(AUserInAServer aUserInAServer);
    void removedPostedImagesIn(AServer aServer);
}
