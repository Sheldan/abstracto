package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.command.models.TableLocks;

public interface LockService {
    void lockTable(TableLocks toLock);
}
