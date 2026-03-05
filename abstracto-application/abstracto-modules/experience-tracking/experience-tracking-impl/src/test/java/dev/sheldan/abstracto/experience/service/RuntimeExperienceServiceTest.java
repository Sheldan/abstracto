package dev.sheldan.abstracto.experience.service;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class RuntimeExperienceServiceTest {

    @Test
    public void shouldCleanExpiredExperience() {
        RunTimeExperienceService experienceService = new RunTimeExperienceService();
        Map<Long, Instant> mapValue = new HashMap<>(Map.of(2L, Instant.now().minusSeconds(5)));
        experienceService.getRuntimeExperience().put(1L, mapValue);
        experienceService.cleanupRunTimeStorage();
        assertThat(experienceService.getRuntimeExperience()).isEmpty();
    }

    @Test
    public void shouldLeaveExperienceIfNotYetExpired() {
        RunTimeExperienceService experienceService = new RunTimeExperienceService();
        Map<Long, Instant> mapValue2 = new HashMap<>(Map.of(3L, Instant.now().plusSeconds(5)));
        experienceService.getRuntimeExperience().put(2L, mapValue2);
        experienceService.cleanupRunTimeStorage();
        assertThat(experienceService.getRuntimeExperience().get(2L)).containsKey(3L);
        assertThat(experienceService.getRuntimeExperience()).hasSize(1);
    }

}
