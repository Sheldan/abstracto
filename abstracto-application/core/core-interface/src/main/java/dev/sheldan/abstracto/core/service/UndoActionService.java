package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.UndoActionInstance;

import java.util.List;

public interface UndoActionService {
    void performActions(List<UndoActionInstance> actionsToPerform);
}
