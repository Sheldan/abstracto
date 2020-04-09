package dev.sheldan.abstracto.utility.config;

import dev.sheldan.abstracto.core.listener.ServerConfigListener;
import dev.sheldan.abstracto.core.models.dto.ServerDto;
import dev.sheldan.abstracto.core.service.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StarboardConfigListener implements ServerConfigListener {

    @Autowired
    private StarboardConfig starboardConfig;

    @Autowired
    private ConfigService configManagementService;

    @Override
    public void updateServerConfig(ServerDto server) {
        for (int i = 0; i < starboardConfig.getLvl().size(); i++) {
            Integer value = starboardConfig.getLvl().get(i);
            configManagementService.createValueIfNotExists("starLvl" + ( i + 1 ), server.getId(), Double.valueOf(value));
        }
    }
}
