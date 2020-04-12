package dev.sheldan.abstracto.experience.config;

import dev.sheldan.abstracto.core.listener.ServerConfigListener;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ExperienceConfigListener implements ServerConfigListener {


    @Autowired
    private ExperienceConfig experienceConfig;

    @Autowired
    private ConfigService service;

    @Override
    public void updateServerConfig(AServer server) {
        log.info("Setting up experience for {}", server.getId());
        service.createDoubleValueIfNotExist("minExp", server.getId(), experienceConfig.getMinExp().doubleValue());
        service.createDoubleValueIfNotExist("maxExp", server.getId(), experienceConfig.getMaxExp().doubleValue());
        service.createDoubleValueIfNotExist("expMultiplier", server.getId(), experienceConfig.getExpMultiplier().doubleValue());
    }
}
