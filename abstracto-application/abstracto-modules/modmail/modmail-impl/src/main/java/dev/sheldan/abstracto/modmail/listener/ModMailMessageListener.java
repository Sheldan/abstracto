package dev.sheldan.abstracto.modmail.listener;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.listener.PrivateMessageReceivedListener;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.service.management.UserManagementService;
import dev.sheldan.abstracto.modmail.config.ModMailFeatures;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import dev.sheldan.abstracto.modmail.service.ModMailThreadService;
import dev.sheldan.abstracto.modmail.service.management.ModMailThreadManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class ModMailMessageListener implements PrivateMessageReceivedListener {

    @Autowired
    private ModMailThreadService modMailThreadService;

    @Autowired
    private ModMailThreadManagementService modMailThreadManagementService;

    @Autowired
    private UserManagementService userManagementService;

    @Override
    @Transactional
    public void execute(Message message) {
        AUser user = userManagementService.loadUser(message.getAuthor().getIdLong());
        ModMailThread existingThread = modMailThreadManagementService.getOpenModmailThreadForUser(user);
        if(existingThread != null) {
            modMailThreadService.relayMessageToModMailThread(existingThread, message);
        } else {
            modMailThreadService.createModMailPrompt(user, message);
        }
    }

    @Override
    public FeatureEnum getFeature() {
        return ModMailFeatures.MOD_MAIL;
    }
}
