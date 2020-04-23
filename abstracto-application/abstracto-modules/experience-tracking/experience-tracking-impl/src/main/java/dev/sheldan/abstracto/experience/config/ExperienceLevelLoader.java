package dev.sheldan.abstracto.experience.config;

import dev.sheldan.abstracto.experience.service.ExperienceLevelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Component responsible to create the amount of {@link dev.sheldan.abstracto.experience.models.database.AExperienceLevel}
 * configured in the {@link ExperienceConfig}. This is executed when the application starts up.
 */
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
        log.info("Setting up experience level configuration.");
        experienceLevelService.createLevelsUntil(maxLevel);
    }
}
