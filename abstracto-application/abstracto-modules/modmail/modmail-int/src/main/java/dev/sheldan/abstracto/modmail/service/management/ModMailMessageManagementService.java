package dev.sheldan.abstracto.modmail.service.management;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.modmail.models.database.ModMailMessage;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;
import java.util.Optional;

/**
 * Management service to handle the creation and retrieval of {@link ModMailMessage} instances from the database
 */
public interface ModMailMessageManagementService {
    /**
     * Creates an instance of {@link ModMailMessage}, attaches it to the given {@link ModMailThread} and returns the created instance
     * @param modMailThread The {@link ModMailThread} the message should be attached to
     * @param createdMessageInDM The {@link Message} which should be attached to the {@link ModMailThread} and was posted to the DM channel (might be null)
     * @param createdMessageInChannel The {@link Message} which should be attached to the {@link ModMailThread} and was posted to the modmail thread (might be null)
     * @param userPostedMessage The {@link Message} which caused this message to be created, the command or the message by the user
     * @param author The {@link AUserInAServer} who authored the {@link Message} originally
     * @param anonymous Whether or not the message was sent anonymous (only possible by staff members)
     * @param dmChannel Whether or not the message originated from the user, and therefore in an direct message channel
     * @return
     */
    ModMailMessage addMessageToThread(ModMailThread modMailThread, Message createdMessageInDM, Message createdMessageInChannel, Message userPostedMessage, AUserInAServer author, Boolean anonymous, Boolean dmChannel);

    /**
     * Retrieves all messages which were sent in a {@link ModMailThread}
     * @param modMailThread The {@link ModMailThread} to retrieve the messages for
     * @return A list of {@link ModMailMessage} which were sent in the given thread
     */
    List<ModMailMessage> getMessagesOfThread(ModMailThread modMailThread);

    Optional<ModMailMessage> getByMessageIdOptional(Long messageId);

    void deleteMessageFromThread(ModMailMessage modMailMessage);
}
