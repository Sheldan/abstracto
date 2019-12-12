package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.AChannel;
import dev.sheldan.abstracto.core.models.PostTarget;

public interface PostTargetService {
    void createPostTarget(String name, AChannel targetChanel);
    void createOrUpdate(String name, AChannel targetChannel);
    void updatePostTarget(PostTarget target, AChannel newTargetChannel);
}
