package dev.sheldan.abstracto.core.service;

import net.dv8tion.jda.api.entities.Message;

public interface MessageService {
    void addReactionToMessage(String emoteKey, Long serverId, Message message);
}
