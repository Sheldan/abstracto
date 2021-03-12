package dev.sheldan.abstracto.assignableroles.service;

import dev.sheldan.abstracto.assignableroles.config.AssignableRolePlaceParameterKey;
import dev.sheldan.abstracto.assignableroles.exception.EmoteNotInAssignableRolePlaceException;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRole;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlacePost;
import dev.sheldan.abstracto.assignableroles.model.database.AssignedRoleUser;
import dev.sheldan.abstracto.core.command.exception.AbstractoTemplatedException;
import dev.sheldan.abstracto.core.exception.ChannelNotInGuildException;
import dev.sheldan.abstracto.core.models.FullEmote;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.concurrent.CompletableFuture;

/**
 * Service responsible to manage actions on an {@link AssignableRolePlace assignableRolePlace}
 */
public interface AssignableRolePlaceService {
    /**
     * Creates a {@link AssignableRolePlace place} with the given parameters
     * @param name The key of the {@link AssignableRolePlace} to be used
     * @param channel The {@link AChannel channel} in which the posts of this {@link AssignableRolePlace place} should be posted, also determines
     *                the {@link AServer server} in which the place should reside in. This {@link AServer server} needs to be unique in combination with the
     *                key
     * @param text The description of the {@link AssignableRolePlace place} which is displayed in the first post of the place
     * @throws dev.sheldan.abstracto.assignableroles.exception.AssignableRoleAlreadyDefinedException if the combination of {@link AServer server}
     * and {@link AssignableRolePlace#key} already exists
     */
    void createAssignableRolePlace(String name, AChannel channel, String text);

    /**
     * Whether or not the {@link AssignableRolePlace place} already has the given {@link AEmote emote} as an {@link AssignableRole role}
     * with the given {@link AEmote emote}
     * @param server The {@link AServer server} in which the {@link AssignableRolePlace place} should be in
     * @param placeName The key of an {@link AssignableRolePlace place} to check
     * @param emote The {@link AEmote emote} which is sought after
     * @return Whether or not the {@link AssignableRolePlace place} already has an {@link AssignableRole role}
     * with the associated {@link AEmote emote}
     */
    boolean hasAssignableRolePlaceEmote(AServer server, String placeName, AEmote emote);

    /**
     * Whether or not the {@link AssignableRolePlace place} (identified by {@link AServer server} and placeName) already has
     * the given {@link AEmote emote} as an {@link AssignableRole role}
     * with the given {@link AEmote emote}
     * @param place The {@link AssignableRolePlace place} in which the {@link AEmote emote} should be sought in
     * @param emote The {@link AEmote emote} which is sought after
     * @return Whether or not the {@link AssignableRolePlace place} already has an {@link AssignableRole role}
     * with the associated {@link AEmote emote}
     */
    boolean hasAssignableRolePlaceEmote(AssignableRolePlace place, AEmote emote);

    /**
     * Whether or not the {@link AssignableRolePlace place} has the position used. Each of the {@link AssignableRole roles}
     * in the {@link AssignableRolePlace place} has a position in which it is posted. This is used to order the roles when posting them. This position is an {@link Integer integer}
     * and it is not guaranteed every position up until the amount of {@link AssignableRole roles} is used.
     * There can be spots, it is only used as an ordering and to swap/move {@link AssignableRole roles}
     * @param server The {@link AServer server} in which the {@link AssignableRolePlace place} should be in
     * @param placeName The key of the {@link AssignableRolePlace place}
     * @param position The position of an {@link AssignableRole role} within the place
     * @return Whether or not there exists an {@link AssignableRole role} within the
     * {@link AssignableRolePlace place} identified by the {@link AServer server} and key
     */
    boolean isPositionUsed(AServer server, String placeName, Integer position);

    /**
     * Sets the {@link AssignableRole role} identified by the
     * {@link FullEmote emote} to the given {@link Integer position} in the {@link AssignableRolePlace place} identified by the
     * {@link AServer server} and key.
     * @param server The {@link AServer server} in which the {@link AssignableRolePlace place} should be
     * @param placeName The key of the {@link AssignableRolePlace place}
     * @param emote The {@link FullEmote emote} which identifies the {@link AssignableRole role}
     *              which position should be changed
     * @param position The new position of the {@link AssignableRole role}
     *                 within the place
     * @throws AbstractoTemplatedException if the position is already used or the {@link FullEmote emote} has not associated {@link AssignableRole role}
     * within the place
     */
    void setEmoteToPosition(AServer server, String placeName, FullEmote emote, Integer position);

    /**
     * Adds the given {@link ARole role} to the {@link AssignableRolePlace place} identified by the {@link AServer server} and key
     * in the form as a {@link AssignableRole role}. This role is identified
     * by the given {@link FullEmote emote} and has the description. If the {@link AssignableRolePlace place} is already setup, this will
     * try to update the message: adding the field and the reaction. This might not always work e.g. the reaction limit was reached.
     * If the update is successful, it will also store the updates in the database.
     * @param server The {@link AServer server} in which the {@link AssignableRolePlace place} is present
     * @param placeName The key of the {@link AssignableRolePlace place}
     * @param role The {@link ARole role} which should be awarded when reacting with the given {@link FullEmote emote}
     * @param emote The {@link FullEmote emote} which should be placed on the {@link Message message}
     *              to  react to and identify the {@link AssignableRole}
     *              for this place
     * @param description The description which will be displayed in the {@link Message message}
     *                    which has the reactions
     * @throws dev.sheldan.abstracto.core.exception.EmoteNotUsableException in case the {@link net.dv8tion.jda.api.entities.Emote emote}
     * cannot be used by the current user. This might be an issue, if its from an external {@link net.dv8tion.jda.api.entities.Guild}
     * the user is not a part of.
     * @return A {@link CompletableFuture future} when the {@link Message message} has been updated
     * and the {@link MessageReaction reaction} has been added. Only tries to execute this, in case there
     * {@link AssignableRolePlacePost posts} known.
     */
    CompletableFuture<Void> addRoleToAssignableRolePlace(AServer server, String placeName, ARole role, FullEmote emote, String description);

    /**
     * Removes the given {@link AssignableRole role} from the {@link AssignableRolePlace place}
     * identified by the {@link AServer server} and the key. The {@link AssignableRole role} is identified
     * by the {@link FullEmote emote} which is associated to it. If there are already {@link AssignableRolePlacePost posts}
     * for this {@link AssignableRolePlace place} this will remove the {@link MessageReaction reaction} and
     * edit the {@link Message message}. If not, the method returns immediately. If the update
     * was successful or was not necessary, the data will be updated in the database as well.
     * @param server The {@link AServer server} in which the {@link AssignableRolePlace place} is
     * @param placeName The key of the {@link AssignableRolePlace place} to remove an {@link AssignableRole role}
     *                  from
     * @param emote The {@link FullEmote fullEmote} which has an {@link AEmote emote} to identify the {@link AssignableRole role}
     *              to remove
     * @return A {@link CompletableFuture future} which completes when the {@link MessageReaction reaction}
     * was removed and the {@link Message message} updated and the database updated, or, if no post is present, only if the database was updated
     */
    CompletableFuture<Void> removeRoleFromAssignableRolePlace(AServer server, String placeName, FullEmote emote);

    /**
     * This method is used to setup the {@link AssignableRolePlacePost} for an
     * {@link AssignableRolePlace place}. If there are previous  {@link AssignableRolePlacePost posts},
     * this method will delete them first. This method does not do the rendering, but is only the entry point for the process.
     * @param server The {@link AServer server} in which the {@link AssignableRolePlace place} is
     * @param name The key which identifies the {@link AssignableRolePlace place} within the {@link AServer server}
     * @return A {@link CompletableFuture future} which completes if the complete setup is finished successfully.
     */
    CompletableFuture<Void> setupAssignableRolePlace(AServer server, String name);

    /**
     * This method is used to update the {@link AssignableRolePlacePost posts}
     *  of the {@link AssignableRolePlace place} identified by the {@link AServer server} and the key. This
     *  effectively re-renders the template for an {@link AssignableRolePlace place} and updates the existing
     *  {@link AssignableRolePlacePost posts} accordingly. In case there are no posts, this method returns immediately.
     * @param server The {@link AServer server} in which the {@link AssignableRolePlace place} is
     * @param name The key of the {@link AssignableRolePlace place}
     * @throws ChannelNotInGuildException if the channel which contained the posts does not exist anymore
     * @return A {@link CompletableFuture future} which completes successfully if *every* update was successful.
     */
    CompletableFuture<Void> refreshAssignablePlacePosts(AServer server, String name);

    /**
     * This method is used to update the {@link AssignableRolePlacePost posts}
     *  of the {@link AssignableRolePlace place}. This effectively re-renders the template for an
     *  {@link AssignableRolePlace place} and updates the existing {@link AssignableRolePlacePost posts} accordingly.
     *  In case there are no posts, this method returns immediately.
     * @param place The {@link AssignableRolePlace place} which should have their {@link Message messages} were updated
     * @throws ChannelNotInGuildException if the channel which contained the posts does not exist anymore
     * @return A {@link CompletableFuture future} which completes successfully if *every* update was successful.
     */
    CompletableFuture<Void> refreshAssignablePlacePosts(AssignableRolePlace place);

    /**
     * This method updates the first {@link AssignableRolePlacePost posts} which contains the description of the
     * {@link AssignableRolePlace place}. If there are no posts, this method will return immediately
     * @param place The {@link AssignableRolePlace place} to update the
     * @throws ChannelNotInGuildException if the channel which contained the post does not exist anymore
     * @return A {@link CompletableFuture future} which completes when the update of the {@link Message message}
     * was completed
     */
    CompletableFuture<Void> refreshTextFromPlace(AssignableRolePlace place);

    /**
     * Sets the active attribute of the {@link AssignableRolePlace place} identified by {@link AServer server}
     * and the key
     * @param server The {@link AServer server} in which the {@link AssignableRolePlace place} is
     * @param name The key of the {@link AssignableRolePlace place}
     * @param newValue The new vale of the active attribute
     */
    void setAssignablePlaceActiveTo(AServer server, String name, Boolean newValue);

    /**
     * Activates the {@link AssignableRolePlace place} identified by {@link AServer server}
     * and the key
     * @param server The {@link AServer server} in which the {@link AssignableRolePlace place} is
     * @param name The key of the {@link AssignableRolePlace place} to activate
     */
    void activateAssignableRolePlace(AServer server, String name);

    /**
     * Activates the {@link AssignableRolePlace place}, which means that the activate attribute will be set to true
     * @param place The {@link AssignableRolePlace place} to activate
     */
    void activateAssignableRolePlace(AssignableRolePlace place);

    /**
     * De-activates the {@link AssignableRolePlace place} identified by {@link AServer server}
     * and the key
     * @param server The {@link AServer server} in which the {@link AssignableRolePlace place} is
     * @param name The key of the {@link AssignableRolePlace place} to de-activate
     */
    void deactivateAssignableRolePlace(AServer server, String name);

    /**
     * De-activates the {@link AssignableRolePlace place}, which means that the activate attribute will be set to false
     * @param place The {@link AssignableRolePlace place} to de-activate
     */
    void deactivateAssignableRolePlace(AssignableRolePlace place);

    /**
     * Sets the inline attribute of the {@link AssignableRolePlace place} identified by {@link AServer server}
     * and the key. This method will update any existing {@link AssignableRolePlacePost posts} of the place, if there
     * are any.
     * @param server The {@link AServer server} in which the {@link AssignableRolePlace place} is
     * @param name The key of the {@link AssignableRolePlace place}
     * @param newValue The new vale of the inline attribute
     */
    CompletableFuture<Void> setAssignablePlaceInlineTo(AServer server, String name, Boolean newValue);

    /**
     * Sets the inline attribute of the {@link AssignableRolePlace place} identified by {@link AServer server}
     * and the key to true. This method will update any existing {@link AssignableRolePlacePost posts} of the place, if there
     * are any.
     * @param server The {@link AServer server} in which the {@link AssignableRolePlace place} is
     * @param name The key of the {@link AssignableRolePlace place} to inline
     */
    CompletableFuture<Void> inlineAssignableRolePlace(AServer server, String name);

    /**
     * Sets the inline attribute of the {@link AssignableRolePlace place} to true. This method will update any existing
     * {@link AssignableRolePlacePost posts} of the place, if there are any.
     * @param place The {@link AssignableRolePlace place} to inline
     */
    CompletableFuture<Void> inlineAssignableRolePlace(AssignableRolePlace place);

    /**
     * Sets the inline attribute of the {@link AssignableRolePlace place} identified by {@link AServer server}
     * and the key to false. This method will update any existing {@link AssignableRolePlacePost posts} of the place, if there
     * are any.
     * @param server The {@link AServer server} in which the {@link AssignableRolePlace place} is
     * @param name The key of the {@link AssignableRolePlace place} to spread
     */
    CompletableFuture<Void> spreadAssignableRolePlace(AServer server, String name);

    /**
     * Sets the inline attribute of the {@link AssignableRolePlace place} to false. This method will update any existing
     * {@link AssignableRolePlacePost posts} of the place, if there are any.
     * @param place The {@link AssignableRolePlace place} to spread
     */
    CompletableFuture<Void> spreadAssignableRolePlace(AssignableRolePlace place);

    /**
     * Sets the unique attribute of the {@link AssignableRolePlace place} identified by {@link AServer server}
     * and the key.
     * @param server The {@link AServer server} in which the {@link AssignableRolePlace place} is
     * @param name The key of the {@link AssignableRolePlace place}
     * @param newValue The new vale of the unique attribute
     */
    void setAssignablePlaceUniqueTo(AServer server, String name, Boolean newValue);

    /**
     * Sets the unique attribute of the {@link AssignableRolePlace place} identified by {@link AServer server}
     * and the key to true.
     * @param server The {@link AServer server} in which the {@link AssignableRolePlace place} is
     * @param name The key of the {@link AssignableRolePlace place} to activate unique role handling
     */
    void uniqueAssignableRolePlace(AServer server, String name);

    /**
     * Sets the unique attribute of the {@link AssignableRolePlace place} to true.
     * @param place The {@link AssignableRolePlace place} to activate unique role handling
     */
    void uniqueAssignableRolePlace(AssignableRolePlace place);

    /**
     * Sets the unique attribute of the {@link AssignableRolePlace place} identified by {@link AServer server}
     * and the key to false.
     * @param server The {@link AServer server} in which the {@link AssignableRolePlace place} is
     * @param name The key of the {@link AssignableRolePlace place} to disable unique role handling
     */
    void multipleAssignableRolePlace(AServer server, String name);

    /**
     * Sets the unique attribute of the {@link AssignableRolePlace place} to false.
     * @param place The {@link AssignableRolePlace place} to disable unique role handling
     */
    void multipleAssignableRolePlace(AssignableRolePlace place);

    /**
     * Sets the auto-remove attribute of the {@link AssignableRolePlace place} identified by {@link AServer server}
     * and the key.
     * @param server The {@link AServer server} in which the {@link AssignableRolePlace place} is
     * @param name The key of the {@link AssignableRolePlace place}
     * @param newValue The new vale of the auto-remove attribute
     */
    void setAssignablePlaceAutoRemoveTo(AServer server, String name, Boolean newValue);

    /**
     * Sets the auto-remove attribute of the {@link AssignableRolePlace place} identified by {@link AServer server}
     * and the key to true.
     * @param server The {@link AServer server} in which the {@link AssignableRolePlace place} is
     * @param name The key of the {@link AssignableRolePlace place} to activate the automatic removal of reactions
     */
    void autoRemoveAssignableRolePlace(AServer server, String name);

    /**
     * Sets the auto-remove attribute of the {@link AssignableRolePlace place} to true.
     * @param place The {@link AssignableRolePlace place} to activate the automatic removal of reactions
     */
    void autoRemoveAssignableRolePlace(AssignableRolePlace place);

    /**
     * Sets the auto-remove attribute of the {@link AssignableRolePlace place} identified by {@link AServer server}
     * and the key to false.
     * @param server The {@link AServer server} in which the {@link AssignableRolePlace place} is
     * @param name The key of the {@link AssignableRolePlace place} to keep reactions
     */
    void keepReactionsAssignableRolePlace(AServer server, String name);

    /**
     * Sets the auto-remove attribute of the {@link AssignableRolePlace place} to false.
     * @param place The {@link AssignableRolePlace place} to keep reactions
     */
    void keepReactionsAssignableRolePlace(AssignableRolePlace place);

    /**
     * Swaps the positions of the two {@link AssignableRole roles} identified by the respective
     * {@link FullEmote emote}. This will only update the it in the database, and require a new setup. The
     * two {@link FullEmote emotes} are assumed to be different
     * @param server The {@link AServer server} in which the {@link AssignableRolePlace place} is
     * @param name The key of the {@link AssignableRolePlace place}
     * @param firstEmote The {@link FullEmote emote} which identifies the first {@link AssignableRole role} to swap
     * @param secondEmote The {@link FullEmote emote} which identifies the second {@link AssignableRole role} to swap
     * @throws EmoteNotInAssignableRolePlaceException if either one of them is not part of the {@link AssignableRolePlace place}
     *
     */
    void swapPositions(AServer server, String name, FullEmote firstEmote, FullEmote secondEmote);

    /**
     * This method renders the {@link AssignableRolePlace place} and post the created {@link MessageToSend messageToSend}
     * in the current channel.
     * @param server The {@link AServer server} of the {@link AssignableRolePlace place} to test
     * @param name The key of the {@link AssignableRolePlace place}
     * @param channel The {@link TextChannel channel} in which the resulting {@link MessageToSend messageToSend}
     *                should be posted in
     * @return A {@link CompletableFuture future} which completes when all {@link MessageToSend messageToSend}
     * have been sent
     */
    CompletableFuture<Void> testAssignableRolePlace(AServer server, String name, TextChannel channel);

    /**
     * This method renders the configuration of the {@link AssignableRolePlace place} identified by {@link AServer server}
     * and key into an {@link MessageToSend messageToSend} and sends a message to the given {@link TextChannel channel}.
     * @param server The {@link AServer server} in which the {@link AssignableRolePlace place} is
     * @param name The key of the {@link AssignableRolePlace place}
     * @param channel The {@link TextChannel channel} in which the resulting {@link MessageToSend messageToSend} should be posted in
     * @return A {@link CompletableFuture future} which completes when the {@link MessageToSend} has been sent
     */
    CompletableFuture<Void> showAssignablePlaceConfig(AServer server, String name, TextChannel channel);

    /**
     * Changes the {@link AChannel channel} of the {@link AssignableRole place} identified by {@link AServer server}
     * and key to the given {@link TextChannel channel}. This only changes the configuration and does not impact
     * any currently posted {@link AssignableRolePlacePost posts}
     * @param name The key of the {@link AssignableRolePlace place}
     * @param newChannel The {@link TextChannel channel} where the {@link AssignableRolePlace place} should be posted to.
     */
    void moveAssignableRolePlace(String name, TextChannel newChannel);

    /**
     * Deletes the {@link AssignableRolePlace place} identified by {@link AServer server} and key. This method will first
     * delete all the {@link AssignableRolePlacePost posts} and then remove the references from the database.
     * @param server The {@link AServer server} in which the {@link AssignableRolePlace place} is
     * @param name The key of the {@link AssignableRolePlace place}
     * @return A {@link CompletableFuture future} which completes after all posts have been deleted
     */
    CompletableFuture<Void> deleteAssignableRolePlace(AServer server, String name);

    /**
     * Changes the text of an {@link AssignableRolePlace place} in the database only.
     * @param server The {@link AServer server} in which the {@link AssignableRolePlace place} is
     * @param name The key of the {@link AssignableRolePlace place}
     * @param newText The new value for the text attribute displayed in the first {@link AssignableRolePlacePost post}
     */
    void changeText(AServer server, String name, String newText);

    /**
     * Changes the text of an {@link AssignableRolePlace place} in the database, and updates the first
     * {@link AssignableRolePlacePost post}. The update of the post happens after the change in the database.
     * @param server The {@link AServer server} in which the {@link AssignableRolePlace place} is
     * @param name The key of the {@link AssignableRolePlace place}
     * @param newText The new value for the text attribute displayed in the first {@link AssignableRolePlacePost post}
     */
    CompletableFuture<Void> changeTextAsync(AServer server, String name, String newText);

    /**
     * Removes the reactions and the roles from the user from the place, this does not touch the stored data
     * @param place The {@link AssignableRolePlace place} to which remove the existing reactions and roles from
     * @param user The {@link AssignedRoleUser user} to the remove *all* reactions and assigned roles from
     * @return A {@link CompletableFuture future} which completes when both of these actions have been done for all {@link AssignableRole assignableRoles}
     */
    CompletableFuture<Void> removeExistingReactionsAndRoles(AssignableRolePlace place, AssignedRoleUser user);

    /**
     * Changes the configuration of the given {@link AssignableRolePlaceParameterKey key} to the given value of the
     * {@link AssignableRolePlace place} identified by {@link AServer server} and key. If it can be updated immediately,
     * this will update the {@link AssignableRolePlacePost posts} and the return {@link CompletableFuture future}
     * will return afterwards. This is the case for {@link AssignableRolePlaceParameterKey#INLINE}. The rest of the
     * keys will only update the configuration in the database, and the place needs a refresh at a later point.
     * @param server The {@link AServer server} in which the {@link AssignableRolePlace place} is
     * @param name The key of the {@link AssignableRolePlace place}
     * @param keyToChange The {@link AssignableRolePlaceParameterKey key} to change
     * @param newValue The new value of the attribute, but be able to convert to a boolean via {@link org.apache.commons.lang3.BooleanUtils#toBooleanObject(String)}
     * @return A {@link CompletableFuture future} which completes when the configuration has been completed
     */
    CompletableFuture<Void> changeConfiguration(AServer server, String name, AssignableRolePlaceParameterKey keyToChange, Object newValue);

    /**
     * Retrieves all {@link AssignableRolePlace places}, renders them into {@link MessageToSend messageToSend} and sends
     * this message to the given {@link TextChannel channel}.
     * @param server The {@link AServer server} for which the {@link AssignableRolePlace places} should be shown for
     * @param channel The {@link TextChannel channel} to send the {@link MessageToSend messageToSend} in
     * @return A {@link CompletableFuture future} which completes when the message has been sent
     */
    CompletableFuture<Void> showAllAssignableRolePlaces(AServer server, TextChannel channel);
}
