package dev.sheldan.abstracto.modmail.service.management;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.modmail.models.database.ModMailMessage;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import net.dv8tion.jda.api.entities.Message;

public interface ModMailMessageManagementService {
    ModMailMessage addMessageToThread(ModMailThread modMailThread, Message message, AUserInAServer author, Boolean anonymous);
}
