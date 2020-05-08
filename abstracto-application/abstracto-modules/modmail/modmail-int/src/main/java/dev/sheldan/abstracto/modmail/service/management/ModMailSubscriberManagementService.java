package dev.sheldan.abstracto.modmail.service.management;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import dev.sheldan.abstracto.modmail.models.database.ModMailThreadSubscriber;

import java.util.List;

public interface ModMailSubscriberManagementService {
    List<ModMailThreadSubscriber> getSubscribersForThread(ModMailThread modMailThread);
    boolean isSubscribedToThread(AUserInAServer aUserInAServer, ModMailThread modMailThread);
    ModMailThreadSubscriber createSubscriber(AUserInAServer aUserInAServer, ModMailThread modMailThread);
    void removeSubscriber(AUserInAServer aUserInAServer, ModMailThread modMailThread);
}
