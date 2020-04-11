package dev.sheldan.abstracto.experience.config;

import dev.sheldan.abstracto.experience.service.ExperienceLevelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ExperienceLevelLoader {

    @Autowired
    private ExperienceConfig experienceConfig;

    @Autowired
    private ExperienceLevelService experienceLevelService;

    @EventListener
    public void handleContextRefreshEvent(ContextRefreshedEvent ctxStartEvt) {
        Integer maxLevel = experienceConfig.getMaxLvl();
        Long experience = 0L;
        experienceLevelService.createExperienceLevel(0, 0L);
        for (int i = 1; i < maxLevel; i++) {
            experience = experience + experienceLevelService.calculateExperienceForLevel(i - 1);
            experienceLevelService.createExperienceLevel(i, experience);
        }
    }
}
