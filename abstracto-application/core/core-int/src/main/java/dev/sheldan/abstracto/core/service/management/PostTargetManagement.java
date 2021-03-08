package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.PostTarget;

import java.util.List;

public interface PostTargetManagement {
    PostTarget createPostTarget(String name, AChannel targetChanel);
    PostTarget createOrUpdate(String name, AChannel targetChannel);
    PostTarget createOrUpdate(String name, AServer server, Long channelId);
    PostTarget createOrUpdate(String name, Long serverId, Long channelId);
    PostTarget getPostTarget(String name, AServer server);
    PostTarget getPostTarget(String name, Long serverId);
    Boolean postTargetExists(String name, AServer server);
    boolean postTargetExists(String name, Long serverId);
    PostTarget updatePostTarget(PostTarget target, AChannel newTargetChannel);
    List<PostTarget> getPostTargetsInServer(AServer server);
}