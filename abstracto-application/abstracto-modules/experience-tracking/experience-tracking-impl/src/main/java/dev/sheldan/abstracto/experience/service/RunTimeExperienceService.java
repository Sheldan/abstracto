package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.core.models.database.AServer;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RunTimeExperienceService {
    private Map<Long, List<AServer>> runtimeExperience = new HashMap<>();

    public Map<Long, List<AServer>> getRuntimeExperience() {
        return runtimeExperience;
    }

}
