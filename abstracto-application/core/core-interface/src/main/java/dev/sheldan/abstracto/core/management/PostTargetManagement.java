package dev.sheldan.abstracto.core.management;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.PostTarget;

public interface PostTargetManagement {
    void createPostTarget(String name, AChannel targetChanel, AServer server);
    void createOrUpdate(String name, AChannel targetChannel, AServer server);
    void createOrUpdate(String name, Long channelId, AServer server);
    void createOrUpdate(String name, Long channelId, Long serverId);
    PostTarget getPostTarget(String name, AServer server);
    PostTarget getPostTarget(String name, Long serverId);
    void updatePostTarget(PostTarget target, AChannel newTargetChannel, AServer server);
}
