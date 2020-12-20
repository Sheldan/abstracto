package dev.sheldan.abstracto.modmail.listener;

import dev.sheldan.abstracto.core.listener.sync.entity.ServerConfigListener;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ConfigManagementService;
import dev.sheldan.abstracto.modmail.service.ModMailThreadServiceBean;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static dev.sheldan.abstracto.modmail.service.ModMailThreadServiceBean.MODMAIL_CLOSING_MESSAGE_TEXT;

/**
 * This listener is used to used to set the initial values of some server specific values, so we don't need to fall
 * back to the default values. The values might not be functional, for example mod mail category id, but their existence
 * makes things easier
 */
@Component
@Slf4j
public class ModMailConfigListener implements ServerConfigListener {


    @Autowired
    private ConfigManagementService configService;

    @Autowired
    private TemplateService templateService;

    @Override
    public void updateServerConfig(AServer server) {
        log.info("Updating modmail related configuration for server {}.", server.getId());
        configService.createIfNotExists(server.getId(), ModMailThreadServiceBean.MODMAIL_CATEGORY, 0L);
        configService.createIfNotExists(server.getId(), MODMAIL_CLOSING_MESSAGE_TEXT, templateService.renderSimpleTemplate("modmail_closing_user_message_description"));
    }
}
