package dev.sheldan.abstracto.modmail.service;

import dev.sheldan.abstracto.modmail.model.database.ModMailMessage;
import dev.sheldan.abstracto.modmail.model.dto.LoadedModmailThreadMessageList;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

/**
 * Service to handle the messages of a {@link dev.sheldan.abstracto.modmail.model.database.ModMailThread}
 */
public interface ModMailMessageService {
    /**
     * Loads the given mod mail messages in the form of {@link Message} from Discord and returns the created promises, some of which might fail, if the message was already deleted
     * @param modMailMessages The list of {@link ModMailMessage} to load
     * @return A instance of {@link LoadedModmailThreadMessageList} which contain the individual results of actively loading the {@link Message} and the {@link net.dv8tion.jda.api.entities.Member}
     */
    LoadedModmailThreadMessageList loadModMailMessages(List<ModMailMessage> modMailMessages);
}
