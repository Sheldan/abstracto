package dev.sheldan.abstracto.core.management;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.PostTarget;

public interface PostTargetManagement {
    void createPostTarget(String name, AServer server, AChannel targetChanel);
    void createOrUpdate(String name, AServer server, AChannel targetChannel);
    void createOrUpdate(String name, AServer server, Long channelId);
    void createOrUpdate(String name, Long serverId, Long channelId);
    PostTarget getPostTarget(String name, AServer server);
    PostTarget getPostTarget(String name, Long serverId);
    void updatePostTarget(PostTarget target, AServer server, AChannel newTargetChannel);
}
