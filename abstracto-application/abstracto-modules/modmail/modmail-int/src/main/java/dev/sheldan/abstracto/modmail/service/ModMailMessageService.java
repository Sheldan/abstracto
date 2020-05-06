package dev.sheldan.abstracto.modmail.service;

import dev.sheldan.abstracto.modmail.models.database.ModMailMessage;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ModMailMessageService {
    List<CompletableFuture<Message>> loadModMailMessages(List<ModMailMessage> modMailMessages);
}
