package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.PostTarget;

public interface PostTargetService {
    void sendTextInPostTarget(String text, PostTarget target);
}
