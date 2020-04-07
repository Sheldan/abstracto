package dev.sheldan.abstracto.utility.service;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.utility.MessageEmbedLink;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public interface MessageEmbedService {
    List<MessageEmbedLink> getLinksInMessage(String message);
    void embedLinks(List<MessageEmbedLink> linksToEmbed, TextChannel target, AUserInAServer reason, Message embeddingMessage);
    void embedLink(CachedMessage cachedMessage, TextChannel target, AUserInAServer cause, Message embeddingMessage);
}
