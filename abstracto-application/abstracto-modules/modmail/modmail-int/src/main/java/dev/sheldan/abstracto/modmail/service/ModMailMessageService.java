package dev.sheldan.abstracto.modmail.service;

import dev.sheldan.abstracto.modmail.models.database.ModMailMessage;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service to handle the messages of a {@link dev.sheldan.abstracto.modmail.models.database.ModMailThread}
 */
public interface ModMailMessageService {
    /**
     * Loads the given mod mail messages in the form of {@link Message} from Discord and returns the created promises, some of which might fail, if the message was already deleted
     * @param modMailMessages The list of {@link ModMailMessage} to load
     * @return A list of futures which contain the individual results of actively loading the {@link Message}
     */
    List<CompletableFuture<Message>> loadModMailMessages(List<ModMailMessage> modMailMessages);
}
