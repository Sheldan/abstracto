package dev.sheldan.abstracto.modmail.service;

import dev.sheldan.abstracto.modmail.model.database.ModMailMessage;
import dev.sheldan.abstracto.modmail.model.template.ModmailLoggingThreadMessages;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ModMailMessageService {
    CompletableFuture<ModmailLoggingThreadMessages> loadModMailMessages(List<ModMailMessage> modMailMessages);
}
