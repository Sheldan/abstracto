package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.UndoActionInstance;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface UndoActionService {
    void performActions(List<UndoActionInstance> actionsToPerform);
    CompletableFuture<Void> performActionsFuture(List<UndoActionInstance> actionsToPerform);
}
