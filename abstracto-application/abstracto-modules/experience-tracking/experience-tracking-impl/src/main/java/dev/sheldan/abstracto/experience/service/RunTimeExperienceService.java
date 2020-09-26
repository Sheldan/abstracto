package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.experience.models.ServerExperience;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RunTimeExperienceService {
    private Map<Long, List<ServerExperience>> runtimeExperience = new HashMap<>();

    public Map<Long, List<ServerExperience>> getRuntimeExperience() {
        return runtimeExperience;
    }

}
