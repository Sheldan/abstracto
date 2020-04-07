package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.exception.EmoteException;
import dev.sheldan.abstracto.core.exception.GuildException;
import dev.sheldan.abstracto.core.service.management.EmoteManagementService;
import dev.sheldan.abstracto.core.models.database.AEmote;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class MessageServiceBean implements MessageService {

    @Autowired
    private Bot bot;

    @Autowired
    private EmoteManagementService emoteManagementService;

    @Override
    public void addReactionToMessage(String emoteKey, Long serverId, Message message) {
        Optional<Guild> guildByIdOptional = bot.getGuildById(serverId);
        Optional<AEmote> aEmote = emoteManagementService.loadEmoteByName(emoteKey, serverId);
        if(guildByIdOptional.isPresent()) {
            Guild guild = guildByIdOptional.get();
            if(aEmote.isPresent()) {
                AEmote emote = aEmote.get();
                if(emote.getCustom()) {
                    Emote emoteById = bot.getInstance().getEmoteById(emote.getEmoteId());
                    if(emoteById != null) {
                        message.addReaction(emoteById).queue();
                    } else {
                        log.error("Emote with key {} and id {} for guild {} was not found.", emoteKey, emote.getEmoteId(), guild.getId());
                        throw new EmoteException(String.format("Emote with key `%s` and id %s in guild %s was not found. Check whether or not the configured emote is available.", emoteKey, emote.getEmoteId(), guild.getIdLong()));
                    }
                } else {
                    message.addReaction(emote.getEmoteKey()).queue();
                }
            } else {
                log.error("Cannot add reaction, emote {} not defined for server {}.", emoteKey, serverId);
                throw new EmoteException(String.format("Cannot add reaction. Emote `%s` not defined in server %s. Define the emote via the setEmote command.", emoteKey, serverId));
            }
        } else {
            log.error("Cannot add reaction, guild not found {}", serverId);
            throw new GuildException(String.format("Cannot add reaction, guild %s not found.", serverId));
        }
    }

    @Override
    public CompletableFuture<Void> deleteMessageInChannelInServer(Long serverId, Long channelId, Long messageId) {
        return bot.deleteMessage(serverId, channelId, messageId);
    }
}
