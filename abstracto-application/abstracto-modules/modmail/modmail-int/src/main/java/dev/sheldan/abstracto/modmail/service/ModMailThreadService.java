package dev.sheldan.abstracto.modmail.service;


import dev.sheldan.abstracto.core.models.FullUser;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public interface ModMailThreadService {
    void createModMailThreadForUser(FullUser userInAServer, MessageChannel feedBackChannel);
    boolean hasOpenThread(AUserInAServer aUserInAServer);
    boolean hasOpenThread(AUser user);
    void setModMailCategoryTo(AServer server, Long categoryId);
    void createModMailPrompt(AUser user, MessageChannel messageChannel);
    void relayMessageToModMailThread(ModMailThread modMailThread, Message message);
    void relayMessageToDm(ModMailThread modMailThread, String text, Message message, Boolean anonymous, MessageChannel feedBack);
    void closeModMailThread(ModMailThread modMailThread, MessageChannel feedBack, String note, Boolean notifyUser);
}
