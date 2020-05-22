package dev.sheldan.abstracto.modmail.listener;

import dev.sheldan.abstracto.core.service.management.DefaultConfigManagementService;
import dev.sheldan.abstracto.templating.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static dev.sheldan.abstracto.modmail.service.ModMailThreadServiceBean.MODMAIL_CLOSING_MESSAGE_TEXT;

@Component
public class ModMailDefaultConfigListener {

    @Autowired
    private DefaultConfigManagementService defaultConfigManagementService;

    @Autowired
    private TemplateService templateService;

    @EventListener
    @Transactional
    public void handleContextRefreshEvent(ContextRefreshedEvent ctxStartEvt) {
        String text = templateService.renderSimpleTemplate("modmail_closing_user_message_description");
        defaultConfigManagementService.createDefaultConfig(MODMAIL_CLOSING_MESSAGE_TEXT, text);

    }
}
