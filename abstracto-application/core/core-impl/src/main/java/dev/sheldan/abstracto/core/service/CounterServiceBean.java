package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.repository.CounterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CounterServiceBean implements CounterService {

    @Autowired
    private CounterRepository counterRepository;

    @Override
    public Long getNextCounterValue(AServer server, String key) {
        return counterRepository.getNewCounterForKey(server.getId(), key);
    }
}
