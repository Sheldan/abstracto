package dev.sheldan.abstracto.modmail.listener;

import dev.sheldan.abstracto.core.listener.ServerConfigListener;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ConfigManagementService;
import dev.sheldan.abstracto.modmail.service.ModMailThreadServiceBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ModMailConfigListener implements ServerConfigListener {

    @Autowired
    private ConfigManagementService configService;

    @Override
    public void updateServerConfig(AServer server) {
        configService.createIfNotExists(server.getId(), ModMailThreadServiceBean.MODMAIL_CATEGORY, 0L);
    }
}
