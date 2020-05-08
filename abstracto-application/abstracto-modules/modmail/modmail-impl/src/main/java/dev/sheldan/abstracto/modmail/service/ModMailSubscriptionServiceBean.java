package dev.sheldan.abstracto.modmail.service;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.modmail.exception.AlreadySubscribedException;
import dev.sheldan.abstracto.modmail.exception.NotSubscribedException;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import dev.sheldan.abstracto.modmail.service.management.ModMailSubscriberManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ModMailSubscriptionServiceBean implements ModMailSubscriptionService {

    @Autowired
    private ModMailSubscriberManagementService modMailSubscriberManagementService;

    @Override
    public void subscribeToThread(AUserInAServer aUserInAServer, ModMailThread modMailThread) {
        if(!modMailSubscriberManagementService.isSubscribedToThread(aUserInAServer, modMailThread)){
            modMailSubscriberManagementService.createSubscriber(aUserInAServer, modMailThread);
        } else {
            throw new AlreadySubscribedException("The user is already subscribed to the thread.");
        }
    }

    @Override
    public void unsubscribeFromThread(AUserInAServer aUserInAServer, ModMailThread modMailThread) {
        if(modMailSubscriberManagementService.isSubscribedToThread(aUserInAServer, modMailThread)){
            modMailSubscriberManagementService.removeSubscriber(aUserInAServer, modMailThread);
        } else {
            throw new NotSubscribedException("The user is not subscribed to the thread.");
        }
    }
}
