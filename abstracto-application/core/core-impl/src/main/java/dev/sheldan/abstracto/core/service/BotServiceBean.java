package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.exception.ChannelNotFoundException;
import dev.sheldan.abstracto.core.exception.GuildException;
import dev.sheldan.abstracto.core.models.GuildChannelMember;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
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
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class BotServiceBean implements BotService {

    public static final String GUILD_NOT_FOUND = "Guild %s not found.";
    private JDA instance;

    @Override
    public void login() throws LoginException {
        JDABuilder builder = new JDABuilder(System.getenv("TOKEN"));

        builder.setDisabledCacheFlags(EnumSet.of(CacheFlag.ACTIVITY));
        builder.setBulkDeleteSplittingEnabled(false);

        this.instance = builder.build();
    }

    @Override
    public JDA getInstance() {
        return instance;
    }

    @Override
    public GuildChannelMember getServerChannelUser(Long serverId, Long channelId, Long userId)  {
        Optional<Guild> guildOptional = getGuildById(serverId);
        if(guildOptional.isPresent()) {
            Guild guild = guildOptional.get();
            Optional<TextChannel> textChannelOptional = this.getTextChannelFromServer(guild, channelId);
            if(textChannelOptional.isPresent()) {
                TextChannel textChannel = textChannelOptional.get();
                Member member = guild.getMemberById(userId);
                return GuildChannelMember.builder().guild(guild).textChannel(textChannel).member(member).build();
            } else {
                throw new ChannelNotFoundException(channelId, serverId);
            }
        }
        else {
            throw new GuildException(String.format(GUILD_NOT_FOUND, serverId));
        }
    }

    @Override
    public Member getMemberInServer(Long serverId, Long memberId) {
        Guild guildById = instance.getGuildById(serverId);
        if(guildById != null) {
            return guildById.getMemberById(memberId);
        } else {
            throw new GuildException(String.format(GUILD_NOT_FOUND, serverId));
        }
    }

    @Override
    public boolean isUserInGuild(AUserInAServer aUserInAServer) {
        Guild guildById = instance.getGuildById(aUserInAServer.getServerReference().getId());
        if(guildById != null) {
            return isUserInGuild(guildById, aUserInAServer);
        } else {
            throw new GuildException(String.format(GUILD_NOT_FOUND, aUserInAServer.getServerReference().getId()));
        }
    }

    @Override
    public boolean isUserInGuild(Guild guild, AUserInAServer aUserInAServer) {
        return guild.getMemberById(aUserInAServer.getUserReference().getId()) != null;
    }

    @Override
    public Member getMemberInServer(AUserInAServer aUserInAServer) {
        return getMemberInServer(aUserInAServer.getServerReference().getId(), aUserInAServer.getUserReference().getId());
    }

    @Override
    public Member getMemberInServer(AServer server, AUser member) {
        return getMemberInServer(server.getId(), member.getId());
    }

    @Override
    public CompletableFuture<Void> deleteMessage(Long serverId, Long channelId, Long messageId)  {
        Optional<TextChannel> textChannelOptional = getTextChannelFromServer(serverId, channelId);
        if(textChannelOptional.isPresent()) {
            TextChannel textChannel = textChannelOptional.get();
            return textChannel.deleteMessageById(messageId).submit();
        } else {
            log.warn("Could not find channel {} in guild {} to delete message {} in.", channelId, serverId, messageId);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> deleteMessage(Long channelId, Long messageId) {
        TextChannel textChannel = getInstance().getTextChannelById(channelId);
        if(textChannel != null) {
            return textChannel.deleteMessageById(messageId).submit();
        } else {
            log.warn("Could not find channel {} to delete message {} in.", channelId, messageId);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public Optional<Emote> getEmote(Long serverId, AEmote emote)  {
        if(!emote.getCustom()) {
            return Optional.empty();
        }
        Optional<Guild> guildById = getGuildById(serverId);
        if(guildById.isPresent()) {
            Guild guild = guildById.get();
            Emote emoteById = guild.getEmoteById(emote.getEmoteId());
            return Optional.ofNullable(emoteById);
        }
        throw new GuildException(String.format(GUILD_NOT_FOUND, serverId));
    }

    @Override
    public Optional<TextChannel> getTextChannelFromServer(Guild guild, Long textChannelId) {
        return Optional.ofNullable(guild.getTextChannelById(textChannelId));
    }

    @Override
    public Optional<TextChannel> getTextChannelFromServer(Long serverId, Long textChannelId)  {
        Optional<Guild> guildOptional = getGuildById(serverId);
        if(guildOptional.isPresent()) {
            Guild guild = guildOptional.get();
            return Optional.ofNullable(guild.getTextChannelById(textChannelId));
        }
        throw new GuildException(String.format(GUILD_NOT_FOUND, serverId));
    }

    @Override
    public Optional<Guild> getGuildById(Long serverId) {
        return Optional.ofNullable(instance.getGuildById(serverId));
    }

    @Override
    public Guild getGuildByIdNullable(Long serverId) {
        return instance.getGuildById(serverId);
    }

    @Override
    public Member getBotInGuild(AServer server) {
        Optional<Guild> guildOptional = getGuildById(server.getId());
        if(guildOptional.isPresent()) {
            Guild guild = guildOptional.get();
            return guild.getMemberById(instance.getSelfUser().getId());
        }
        return null;
    }

    @Override
    public void shutdown() {

    }
}
