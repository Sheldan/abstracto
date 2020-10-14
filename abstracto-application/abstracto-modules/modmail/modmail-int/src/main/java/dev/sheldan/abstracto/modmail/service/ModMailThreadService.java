package dev.sheldan.abstracto.modmail.service;


import dev.sheldan.abstracto.core.models.UndoActionInstance;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service used to handle the mod mail life cycle, including creation, updating, sending/receiving messages and logging the mod mail thread
 */
public interface ModMailThreadService {
    /**
     * Creates a new mod mail thread for the given user. including: the {@link net.dv8tion.jda.api.entities.TextChannel}
     * in the appropriate {@link net.dv8tion.jda.api.entities.Category} and calls the methods responsible for storing
     * the necessary data in the database, notifying the users and sending messages  related to the creation of the {@link ModMailThread}
     * @param member The {@link AUserInAServer} to create the mod mail thread for
     * @param initialMessage The initial message sparking this mod mail thread, null in case it was created by a command
     * @param feedBackChannel The {@link MessageChannel} in which feedback about exceptions should be posted to
     * @param userInitiated Whether or not the mod mail thread was initiated by a user
     */
    CompletableFuture<Void> createModMailThreadForUser(Member member, Message initialMessage, MessageChannel feedBackChannel, boolean userInitiated, List<UndoActionInstance> undoActions);

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
     * @param message The {@link Message} object which was sent by the user to answer with
     */
    CompletableFuture<Message> relayMessageToModMailThread(ModMailThread modMailThread, Message message, List<UndoActionInstance> undoActions);

    /**
     * Forwards a message send by a moderator to the direct message channel opened with the user. If the message is
     * marked as anonymous, the bot will take the place of the author, in other case the author is shown in the embed.
     * @param threadId The id of the {@link ModMailThread} to which the reply was sent to
     * @param text The parsed text of the reply
     * @param message  The pure {@link Message} containing the command which caused the reply
     * @param anonymous Whether or nor the message should be send anonymous
     * @param feedBack The {@link MessageChannel} in which feedback about possible exceptions should be sent to
     * @param undoActions list of {@link UndoActionInstance} to execute in case this fails
     * @param targetMember The {@link Member} the {@link ModMailThread} is about.
     */
    CompletableFuture<Void> relayMessageToDm(Long threadId, String text, Message message, boolean anonymous, MessageChannel feedBack, List<UndoActionInstance> undoActions, Member targetMember);

    /**
     * Closes the mod mail thread which means: deletes the {@link net.dv8tion.jda.api.entities.TextChannel} associated with the mod mail thread,
     * and depending on the {@link dev.sheldan.abstracto.core.config.FeatureMode} of mod mail logs the content of the thread into the appropriate
     * post target. This also takes an optional note, which will be displayed in the first message of the logging. This method changes the state of the
     * {@link ModMailThread} to CLOSED and notifies the user about closing.
     * @param modMailThread The {@link ModMailThread} which is being closed.
     * @param note The text of the note used for the header message of the logged mod mail thread.
     * @param notifyUser Whether or not the user should be notified
     * @param log whether or not the closed {@link ModMailThread} should be logged (if the {@link dev.sheldan.abstracto.core.config.FeatureMode} is enabled)
     */
    CompletableFuture<Void> closeModMailThread(ModMailThread modMailThread, String note, boolean notifyUser, List<UndoActionInstance> undoActions, Boolean log);

    /**
     * Closes the mod mail thread which means: deletes the {@link net.dv8tion.jda.api.entities.TextChannel} associated with the mod mail thread,
     * and logs the content of the thread into the appropriate post target. This also takes an optional note, which will
     * be displayed in the first message of the logging. This method changes the state of the {@link ModMailThread} to
     * CLOSED and notifies the user about closing.
     * @param modMailThread The {@link ModMailThread} which is being closed.
     * @param note The text of the note used for the header message of the logged mod mail thread, this is only required when actually
     *             logging the mod mail thread
     * @param notifyUser Whether or not the user should be notified
     * @param logThread Whether or not the thread should be logged to the appropriate post target
     */
    CompletableFuture<Void> closeModMailThread(ModMailThread modMailThread, String note, boolean notifyUser, boolean logThread, List<UndoActionInstance> undoActions);
}
