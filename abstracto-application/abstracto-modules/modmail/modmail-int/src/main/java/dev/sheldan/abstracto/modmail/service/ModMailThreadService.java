package dev.sheldan.abstracto.modmail.service;


import dev.sheldan.abstracto.core.models.UndoActionInstance;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.modmail.model.ClosingContext;
import dev.sheldan.abstracto.modmail.model.database.ModMailThread;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service used to handle the mod mail life cycle, including creation, updating, sending/receiving messages and logging the mod mail thread
 */
public interface ModMailThreadService {
    /**
     * Creates a new mod mail thread for the given user. including: the {@link net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel}
     * in the appropriate {@link net.dv8tion.jda.api.entities.channel.concrete.Category} and calls the methods responsible for storing
     * the necessary data in the database, notifying the users and sending messages  related to the creation of the {@link ModMailThread}
     * @param user The {@link User} to create the mod mail thread for
     * @param guild The {@link Guild} in which the mod mail thread should be created in
     * @param initialMessage The initial message sparking this mod mail thread, null in case it was created by a command
     * @param userInitiated Whether the mod mail thread was initiated by a user
     * @param undoActions A list of {@link dev.sheldan.abstracto.core.models.UndoAction actions} to be undone in case the operation fails. This list will be filled in the method.
     * @param appeal Whether the modmail thread was created for the purpose of an appeal
     * @return A {@link CompletableFuture future} which completes when the modmail thread is set up
     */
    CompletableFuture<MessageChannel> createModMailThreadForUser(User user, Guild guild, Message initialMessage, boolean userInitiated, List<UndoActionInstance> undoActions, boolean appeal);

    CompletableFuture<Void> sendContactNotification(User user, MessageChannel createdMessageChannel, MessageChannel feedBackChannel);
    CompletableFuture<Void> sendContactNotification(User user, MessageChannel createdMessageChannel, InteractionHook interactionHook);

    /**
     * Changes the configuration value of the category used to create mod mail threads to the given ID.
     * @param guild The {@link Guild} to change the category value for
     * @param categoryId The ID of the category to use
     */
    void setModMailCategoryTo(Guild guild, Long categoryId);

    /**
     * Creates a prompt message, where the user initiating the mod mail thread has to chose for which server
     * the user wants to create a mod mail thread for. The message will be displayed in the same channel
     * as the original message was displayed in. Only servers in which the mod mail feature is enabled, will be counted
     * and if there are no servers available an error message will be displayed.
     * @param user The {@link AUser} who wants to open a mod mail thread
     * @param initialMessage The {@link Message} which was send by the user to open a mod mail thread with.
     */
    void createModMailPrompt(AUser user, Message initialMessage);

    /**
     * Forwards the given {@link Message} send by the user to the appropriate text channel of the given {@link ModMailThread}.
     * In case there was no channel found, this will cause a message to be shown to the user and the existing mod mail thread will be closed.
     * This is the case, if the mod mail thread was still open in the database, but no text channel was found anymore.
     * @param modMailThread The {@link ModMailThread} on which the user answered
     * @param undoActions A list of {@link dev.sheldan.abstracto.core.models.UndoAction actions} to be undone in case the operation fails. This list will be filled in the method.
     * @param messageFromUser The {@link Message} object which was sent by the user as an answer
     * @return A {@link CompletableFuture future} which completes when the message has been relayed to the channel
     */
    CompletableFuture<Message> relayMessageToModMailThread(ModMailThread modMailThread, Message messageFromUser, List<UndoActionInstance> undoActions);

    /**
     * Forwards a message send by a moderator to the direct message channel opened with the user. If the message is
     * marked as anonymous, the bot will take the place of the author, in other case the author is shown in the embed.
     * @param threadId The id of the {@link ModMailThread} to which the reply was sent to
     * @param text The parsed text of the reply
     * @param message  The pure {@link Message} containing the command which caused the reply
     * @param anonymous Whether or nor the message should be send anonymous
     * @param targetUser The {@link User} the {@link ModMailThread} is about.
     * @param guild The guild the reply is created in
     * @return A {@link CompletableFuture future} which completes when the message has been relayed to the DM
     */
    CompletableFuture<Void> loadExecutingMemberAndRelay(Long threadId, String text, Message message, boolean anonymous, User targetUser, Guild guild);

    /**
     * Closes the mod mail thread which means: deletes the {@link net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel} associated with the mod mail thread,
     * and depending on the {@link dev.sheldan.abstracto.core.config.FeatureMode} of mod mail logs the content of the thread into the appropriate
     * post target. This also takes an optional note, which will be displayed in the first message of the logging. This method changes the state of the
     * {@link ModMailThread} to CLOSED and notifies the user about closing.
     * @param modMailThread The {@link ModMailThread} which is being closed.
     * @param undoActions A list of {@link dev.sheldan.abstracto.core.models.UndoAction actions} to be undone in case the operation fails. This list will be filled in the method.
     * @return A {@link CompletableFuture future} which completes when the {@link ModMailThread thread} has been closed.
     */
    CompletableFuture<Void> closeModMailThreadEvaluateLogging(ModMailThread modMailThread, ClosingContext closingConfig, List<UndoActionInstance> undoActions);

    /**
     * Closes the mod mail thread which means: deletes the {@link net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel} associated with the mod mail thread,
     * and logs the content of the thread into the appropriate post target. This also takes an optional note, which will
     * be displayed in the first message of the logging. This method changes the state of the {@link ModMailThread} to
     * CLOSED and notifies the user about closing.
     * @param modMailThread The {@link ModMailThread} which is being closed.
     * @param closingConfig The {@link ClosingContext config} how the thread shoudl be closed
     * @param undoActions A list of {@link dev.sheldan.abstracto.core.models.UndoAction actions} to be undone in case the operation fails. This list will be filled in the method.
     * @return A {@link CompletableFuture future} which completes when the {@link ModMailThread thread} has been closed
     */
    CompletableFuture<Void> closeModMailThread(ModMailThread modMailThread, ClosingContext closingConfig, List<UndoActionInstance> undoActions);

    boolean isModMailThread(AChannel channel);
    boolean isModMailThread(Long channelId);

    CompletableFuture<Void> rejectAppeal(ModMailThread modMailThread, String reason, Member memberPerforming);
}
