package dev.sheldan.abstracto.modmail.service.management;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import dev.sheldan.abstracto.modmail.models.database.ModMailThreadSubscriber;

import java.util.List;

/**
 * Management service used to retrieve/create/remove instances of {@link ModMailThreadSubscriber}
 */
public interface ModMailSubscriberManagementService {
    /**
     * Retrieves all the {@link ModMailThreadSubscriber} of the given {@link ModMailThread}
     * @param modMailThread The {@link ModMailThread} to retrieve the subscribers of
     * @return The list of {@link ModMailThreadSubscriber} which are currently configured for the given {@link ModMailThread}
     */
    List<ModMailThreadSubscriber> getSubscribersForThread(ModMailThread modMailThread);

    /**
     * This method checks if a {@link AUserInAServer} is stored as a {@link ModMailThreadSubscriber} for the given
     * {@link ModMailThread}
     * @param aUserInAServer The {@link AUserInAServer} to check for
     * @param modMailThread The {@link ModMailThread} to check in
     * @return Whether or not the given {@link AUserInAServer} is a subscriber of the mod mail thread
     */
    boolean isSubscribedToThread(AUserInAServer aUserInAServer, ModMailThread modMailThread);

    /**
     * Creates a {@link ModMailThreadSubscriber} with the given parameters and returns the created instance
     * @param aUserInAServer The {@link AUserInAServer} to subscribe to a thread
     * @param modMailThread The {@link ModMailThread} to which the user should be subscribed to
     * @return The created instance of {@link ModMailThreadSubscriber}
     */
    ModMailThreadSubscriber createSubscriber(AUserInAServer aUserInAServer, ModMailThread modMailThread);

    /**
     * Removes the instance of the {@link ModMailThreadSubscriber}, effectively un-subscribing the user from the thread
     * @param aUserInAServer The {@link AUserInAServer} to un-subscribe
     * @param modMailThread The {@link ModMailThread} to unsubscribe from
     */
    void removeSubscriber(AUserInAServer aUserInAServer, ModMailThread modMailThread);
}
