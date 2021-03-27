package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.command.model.TableLocks;

public interface LockService {
    void lockTable(TableLocks toLock);
}
