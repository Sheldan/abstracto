package dev.sheldan.abstracto.modmail.service;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;

public interface ModMailSubscriptionService {
    void subscribeToThread(AUserInAServer aUserInAServer, ModMailThread modMailThread);
    void unsubscribeFromThread(AUserInAServer aUserInAServer, ModMailThread modMailThread);
}
