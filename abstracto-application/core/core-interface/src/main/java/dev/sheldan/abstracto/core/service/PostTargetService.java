package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.database.PostTarget;

public interface PostTargetService {
    void sendTextInPostTarget(String text, PostTarget target);
    void sendTextInPostTarget(String text, String postTargetName, Long serverId);
}
