package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.exception.NotFoundException;
import dev.sheldan.abstracto.core.models.ServerChannelUser;
import dev.sheldan.abstracto.core.models.database.AEmote;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;
import java.util.EnumSet;
import java.util.Optional;

@Service
@Slf4j
public class BotService implements Bot {

    private JDA instance;

    @Override
    public void login() throws LoginException {
        JDABuilder builder = new JDABuilder(System.getenv("TOKEN"));

        builder.setDisabledCacheFlags(EnumSet.of(CacheFlag.ACTIVITY, CacheFlag.VOICE_STATE));
        builder.setBulkDeleteSplittingEnabled(false);

        this.instance = builder.build();
    }

    @Override
    public JDA getInstance() {
        return instance;
    }

    @Override
    public ServerChannelUser getServerChannelUser(Long serverId, Long channelId, Long userId) {
        Optional<Guild> guildOptional = getGuildById(serverId);
        if(guildOptional.isPresent()) {
            Guild guild = guildOptional.get();
            Optional<TextChannel> textChannelOptional = this.getTextChannelFromServer(guild, channelId);
            if(textChannelOptional.isPresent()) {
                TextChannel textChannel = textChannelOptional.get();
                Member member = guild.getMemberById(userId);
                return ServerChannelUser.builder().guild(guild).textChannel(textChannel).member(member).build();
            } else {
                throw new NotFoundException(String.format("Text channel %s not found in guild %s", channelId, serverId));
            }
        }
        else {
            throw new NotFoundException(String.format("Guild %s not found.", serverId));
        }
    }



    @Override
    public Member getMemberInServer(Long serverId, Long memberId) {
        Guild guildById = instance.getGuildById(serverId);
        if(guildById != null) {
            return guildById.getMemberById(memberId);
        } else {
            throw new RuntimeException(String.format("Member %s not found in guild %s", memberId, serverId));
        }
    }

    @Override
    public void deleteMessage(Long serverId, Long channelId, Long messageId) {
        Optional<TextChannel> textChannelOptional = getTextChannelFromServer(serverId, channelId);
        if(textChannelOptional.isPresent()) {
            TextChannel textChannel = textChannelOptional.get();
            textChannel.deleteMessageById(messageId).queue(aVoid -> {}, throwable -> {
                log.warn("Failed to delete message {} in channel {} in guild {}", messageId, channelId, serverId, throwable);
            });
        } else {
            log.warn("Could not find channel {} in guild {} to delete message {} in.", channelId, serverId, messageId);
        }
    }

    @Override
    public Optional<Emote> getEmote(Long serverId, AEmote emote) {
        if(!emote.getCustom()) {
            return Optional.empty();
        }
        Optional<Guild> guildById = getGuildById(serverId);
        if(guildById.isPresent()) {
            Guild guild = guildById.get();
            return Optional.ofNullable(guild.getEmoteById(emote.getEmoteId()));
        }
        throw new NotFoundException(String.format("Not able to find emote %s in server %s", emote.getId(), serverId));
    }

    @Override
    public Optional<TextChannel> getTextChannelFromServer(Guild guild, Long textChannelId) {
        return Optional.ofNullable(guild.getTextChannelById(textChannelId));
    }

    @Override
    public Optional<TextChannel> getTextChannelFromServer(Long serverId, Long textChannelId) {
        Optional<Guild> guildOptional = getGuildById(serverId);
        if(guildOptional.isPresent()) {
            Guild guild = guildOptional.get();
            return Optional.ofNullable(guild.getTextChannelById(textChannelId));
        }
        throw new NotFoundException(String.format("Not able to find guild %s", serverId));
    }

    @Override
    public Optional<Guild> getGuildById(Long serverId) {
        return Optional.ofNullable(instance.getGuildById(serverId));
    }

    @Override
    public void shutdown() {

    }
}
