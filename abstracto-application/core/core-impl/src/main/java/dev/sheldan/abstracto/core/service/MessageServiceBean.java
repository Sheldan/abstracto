package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.exception.EmoteException;
import dev.sheldan.abstracto.core.exception.GuildException;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.service.management.EmoteManagementService;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.templating.model.MessageToSend;
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
    private BotService botService;

    @Autowired
    private EmoteManagementService emoteManagementService;

    @Autowired
    private EmoteService emoteService;

    @Autowired
    private ChannelService channelService;

    @Override
    public void addReactionToMessage(String emoteKey, Long serverId, Message message) {
        Optional<Guild> guildByIdOptional = botService.getGuildById(serverId);
        Optional<AEmote> aEmote = emoteManagementService.loadEmoteByName(emoteKey, serverId);
        if(guildByIdOptional.isPresent()) {
            Guild guild = guildByIdOptional.get();
            if(aEmote.isPresent()) {
                AEmote emote = aEmote.get();
                if(emote.getCustom()) {
                    Emote emoteById = botService.getInstance().getEmoteById(emote.getEmoteId());
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
                String defaultEmote = emoteService.getDefaultEmote(emoteKey);
                message.addReaction(defaultEmote).queue();}
        } else {
            log.error("Cannot add reaction, guild not found {}", serverId);
            throw new GuildException(String.format("Cannot add reaction, guild %s not found.", serverId));
        }
    }

    @Override
    public CompletableFuture<Void> deleteMessageInChannelInServer(Long serverId, Long channelId, Long messageId) {
        return botService.deleteMessage(serverId, channelId, messageId);
    }

    @Override
    public CompletableFuture<Message> createStatusMessage(MessageToSend messageToSend, AChannel channel) {
        return channelService.sendMessageToEndInAChannel(messageToSend, channel).get(0);
    }

    @Override
    public void updateStatusMessage(AChannel channel, Long messageId, MessageToSend messageToSend) {
        channelService.editMessageInAChannel(messageToSend, channel, messageId);
    }
}
