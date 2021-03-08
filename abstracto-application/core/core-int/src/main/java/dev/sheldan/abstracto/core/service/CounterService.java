package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.database.AServer;

public interface CounterService {
    Long getNextCounterValue(AServer server, String key);
}
