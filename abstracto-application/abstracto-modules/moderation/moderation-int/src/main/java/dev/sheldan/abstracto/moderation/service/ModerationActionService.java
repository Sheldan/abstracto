package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.moderation.model.ModerationActionButton;

import java.util.List;

public interface ModerationActionService {
    List<ModerationActionButton> getModerationActionButtons(ServerUser serverUser);
}
