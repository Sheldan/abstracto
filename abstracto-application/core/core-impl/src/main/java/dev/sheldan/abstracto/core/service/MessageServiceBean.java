package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.management.EmoteManagementService;
import dev.sheldan.abstracto.core.models.database.AEmote;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MessageServiceBean implements MessageService {

    @Autowired
    private Bot bot;

    @Autowired
    private EmoteManagementService emoteManagementService;

    @Override
    public void addReactionToMessage(String emoteKey, Long serverId, Message message) {
        Guild guildById = bot.getGuildById(serverId);
        AEmote emote = emoteManagementService.loadEmoteByName(emoteKey, serverId);
        if(emote.getCustom()) {
            Emote emoteById = guildById.getEmoteById(emote.getEmoteId());
            if(emoteById != null) {
                message.addReaction(emoteById).queue();
            } else {
                log.warn("Emote with key {} and id {} for guild {} was not found.", emoteKey, emote.getEmoteId(), guildById.getId());
            }
        } else {
            message.addReaction(emote.getEmoteKey()).queue();
        }
    }
}
