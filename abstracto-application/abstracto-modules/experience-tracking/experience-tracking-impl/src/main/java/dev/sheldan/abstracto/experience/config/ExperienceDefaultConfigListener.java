package dev.sheldan.abstracto.experience.config;

import dev.sheldan.abstracto.core.service.management.DefaultConfigManagementService;
import dev.sheldan.abstracto.experience.config.features.ExperienceFeatureConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
public class ExperienceDefaultConfigListener {

    @Autowired
    private DefaultConfigManagementService defaultConfigManagementService;

    @Autowired
    private ExperienceConfig experienceConfig;

    @EventListener
    @Transactional
    public void handleContextRefreshEvent(ContextRefreshedEvent ctxStartEvt) {
        defaultConfigManagementService.createDefaultConfig(ExperienceFeatureConfig.MIN_EXP_KEY, experienceConfig.getMinExp().longValue());
        defaultConfigManagementService.createDefaultConfig(ExperienceFeatureConfig.MAX_EXP_KEY, experienceConfig.getMaxExp().longValue());
        defaultConfigManagementService.createDefaultConfig(ExperienceFeatureConfig.EXP_MULTIPLIER_KEY, experienceConfig.getExpMultiplier());
    }
}
