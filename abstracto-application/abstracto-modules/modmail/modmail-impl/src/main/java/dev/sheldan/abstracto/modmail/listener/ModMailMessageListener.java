package dev.sheldan.abstracto.modmail.listener;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.listener.sync.jda.PrivateMessageReceivedListener;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserManagementService;
import dev.sheldan.abstracto.modmail.config.ModMailFeatures;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import dev.sheldan.abstracto.modmail.service.ModMailThreadService;
import dev.sheldan.abstracto.modmail.service.management.ModMailThreadManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

/**
 * This listener is the core mechanic behind mod mail, if the bot receives a message via DM, this listener is executed
 * and checks if the message should be forwarded to an existing mod mail thread, or if a new thread should be created/the
 * user should be prompted for a new mod mail thread.
 */
@Component
@Slf4j
public class ModMailMessageListener implements PrivateMessageReceivedListener {

    @Autowired
    private ModMailThreadService modMailThreadService;

    @Autowired
    private ModMailThreadManagementService modMailThreadManagementService;

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Override
    @Transactional
    public void execute(Message message) {
        if(message.getAuthor().isBot()) {
            return;
        }
        AUser user = userManagementService.loadOrCreateUser(message.getAuthor().getIdLong());
        if(modMailThreadManagementService.hasOpenModMailThread(user)) {
            log.trace("User {} has an open modmail thread. Forwarding message {}.", user.getId(), message.getId());
            // there is only one open mod mail thread for a user at a time, so we can select the first one
            // we cannot use the AUserInAServer directly, because a message in a private channel does not have a Member
            ModMailThread existingThread = modMailThreadManagementService.getOpenModMailThreadsForUser(user).get(0);
            modMailThreadService.relayMessageToModMailThread(existingThread, message, new ArrayList<>()).exceptionally(throwable -> {
                log.error("Error when relaying message from user {} in modmail listener.", message.getAuthor().getIdLong(), throwable);
                return null;
            });
        } else {
            log.info("User {} does not have an open modmail thread. Crating prompt.", user.getId());
            modMailThreadService.createModMailPrompt(user, message);
        }
    }

    @Override
    public FeatureEnum getFeature() {
        return ModMailFeatures.MOD_MAIL;
    }

    @Override
    public Integer getPriority() {
        return ListenerPriority.HIGH;
    }
}
