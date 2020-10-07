package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.repository.CounterRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CounterServiceBean implements CounterService {

    @Autowired
    private CounterRepository counterRepository;

    @Override
    public Long getNextCounterValue(AServer server, String key) {
        log.trace("Retrieving new counter value for key {} in server {}.", key, server.getId());
        return counterRepository.getNewCounterForKey(server.getId(), key);
    }
}
