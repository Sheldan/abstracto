package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.models.CachedMessage;

public interface MessageDeletedListener {
    void execute(CachedMessage messageBefore);
}
