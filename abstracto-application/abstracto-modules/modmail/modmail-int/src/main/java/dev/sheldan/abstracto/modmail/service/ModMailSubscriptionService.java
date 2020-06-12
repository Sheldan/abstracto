package dev.sheldan.abstracto.modmail.service;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;

/**
 * Service used to add subscriptions to threads and remove them as well.
 */
public interface ModMailSubscriptionService {
    /**
     * Subscribes the {@link AUserInAServer} to the given {@link ModMailThread}.
     * @param aUserInAServer The {@link AUserInAServer} to subscribe
     * @param modMailThread The {@link ModMailThread} to subscribe the user to
     * @throws dev.sheldan.abstracto.modmail.exception.AlreadySubscribedException in case the user is already subscribed
     */
    void subscribeToThread(AUserInAServer aUserInAServer, ModMailThread modMailThread);

    /**
     * Un-subscribes the {@link AUserInAServer} from the given {@link ModMailThread}
     * @param aUserInAServer The {@link AUserInAServer} to un-subscribe
     * @param modMailThread The {@link ModMailThread} to un-subscribe the user from
     * @throws dev.sheldan.abstracto.modmail.exception.NotSubscribedException in case the user is not subscribed to the thread
     */
    void unsubscribeFromThread(AUserInAServer aUserInAServer, ModMailThread modMailThread);
}
