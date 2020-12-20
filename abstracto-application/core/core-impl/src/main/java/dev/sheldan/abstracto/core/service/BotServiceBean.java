package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.exception.ChannelNotInGuildException;
import dev.sheldan.abstracto.core.exception.GuildNotFoundException;
import dev.sheldan.abstracto.core.models.GuildChannelMember;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static net.dv8tion.jda.api.requests.GatewayIntent.*;

@Service
@Slf4j
public class BotServiceBean implements BotService {

    private JDA instance;

    @Override
    public void login() throws LoginException {
        JDABuilder builder = JDABuilder.create(System.getenv("TOKEN"), GatewayIntent.GUILD_MEMBERS, GUILD_VOICE_STATES,
                GUILD_EMOJIS, GUILD_MEMBERS, GUILD_MESSAGE_REACTIONS, GUILD_MESSAGES,
                GUILD_MESSAGE_REACTIONS, DIRECT_MESSAGE_REACTIONS, DIRECT_MESSAGES, GUILD_PRESENCES);

        builder.setBulkDeleteSplittingEnabled(false);
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);

        this.instance = builder.build();
    }

    @Override
    public JDA getInstance() {
        return instance;
    }

    @Override
    public GuildChannelMember getServerChannelUser(Long serverId, Long channelId, Long userId)  {
        log.trace("Trying to retrieve member {}, channel {} in server {} from cache.", userId, channelId, serverId);
        Guild guild = getGuildById(serverId);
            Optional<TextChannel> textChannelOptional = this.getTextChannelFromServerOptional(guild, channelId);
            if(textChannelOptional.isPresent()) {
                TextChannel textChannel = textChannelOptional.get();
                Member member = guild.getMemberById(userId);
                return GuildChannelMember.builder().guild(guild).textChannel(textChannel).member(member).build();
            } else {
                throw new ChannelNotInGuildException(channelId);
            }
    }

    @Override
    public CompletableFuture<GuildChannelMember> getServerChannelUserAsync(Long serverId, Long channelId, Long userId) {
        log.trace("Trying to retrieve member {}, channel {} in server {} async.", userId, channelId, serverId);
        CompletableFuture<Member> memberFuture = getMemberInServerAsync(serverId, userId);

        Guild guild = getGuildById(serverId);
        TextChannel textChannel = this.getTextChannelFromServer(guild, channelId);
        return memberFuture.thenApply(member ->
            GuildChannelMember.builder().guild(guild).textChannel(textChannel).member(member).build()
        );
    }

    @Override
    public Member getMemberInServer(Long serverId, Long memberId) {
        log.trace("Retrieving member {} in server {} from cache.", memberId, serverId);
        Guild guildById = instance.getGuildById(serverId);
        if(guildById != null) {
            return guildById.getMemberById(memberId);
        } else {
            throw new GuildNotFoundException(serverId);
        }
    }

    @Override
    public CompletableFuture<Member> getMemberInServerAsync(Long serverId, Long memberId) {
        log.trace("Retrieving member {} in server {} from cache.", memberId, serverId);
        Guild guildById = instance.getGuildById(serverId);
        if(guildById != null) {
            return guildById.retrieveMemberById(memberId).submit();
        } else {
            throw new GuildNotFoundException(serverId);
        }
    }

    @Override
    public CompletableFuture<Member> retrieveMemberInServer(ServerUser serverUser) {
        return getMemberInServerAsync(serverUser.getServerId(), serverUser.getUserId());
    }

    @Override
    public CompletableFuture<User> retrieveUserById(Long userId) {
        return instance.retrieveUserById(userId).submit();
    }

    @Override
    public boolean isUserInGuild(AUserInAServer aUserInAServer) {
        Guild guildById = instance.getGuildById(aUserInAServer.getServerReference().getId());
        if(guildById != null) {
            return isUserInGuild(guildById, aUserInAServer);
        } else {
            throw new GuildNotFoundException(aUserInAServer.getServerReference().getId());
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
    public CompletableFuture<Member> getMemberInServerAsync(AUserInAServer aUserInAServer) {
        return getMemberInServerAsync(aUserInAServer.getServerReference().getId(), aUserInAServer.getUserReference().getId());
    }

    @Override
    public Member getMemberInServer(AServer server, AUser member) {
        return getMemberInServer(server.getId(), member.getId());
    }

    @Override
    public CompletableFuture<Void> deleteMessage(Long serverId, Long channelId, Long messageId)  {
        Optional<TextChannel> textChannelOptional = getTextChannelFromServerOptional(serverId, channelId);
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
    public CompletableFuture<Member> forceReloadMember(Member member) {
        return member.getGuild().retrieveMember(member.getUser()).submit();
    }

    @Override
    public Optional<Emote> getEmote(Long serverId, AEmote emote)  {
        if(Boolean.FALSE.equals(emote.getCustom())) {
            return Optional.empty();
        }
        Optional<Guild> guildById = getGuildByIdOptional(serverId);
        if(guildById.isPresent()) {
            Guild guild = guildById.get();
            Emote emoteById = guild.getEmoteById(emote.getEmoteId());
            return Optional.ofNullable(emoteById);
        }
        throw new GuildNotFoundException(serverId);
    }

    @Override
    public Optional<Emote> getEmote(AEmote emote) {
        if(Boolean.FALSE.equals(emote.getCustom())) {
            return Optional.empty();
        }
        return Optional.ofNullable(instance.getEmoteById(emote.getEmoteId()));
    }

    @Override
    public Optional<TextChannel> getTextChannelFromServerOptional(Guild guild, Long textChannelId) {
        return Optional.ofNullable(guild.getTextChannelById(textChannelId));
    }

    @Override
    public TextChannel getTextChannelFromServer(Guild guild, Long textChannelId) {
        return getTextChannelFromServerOptional(guild, textChannelId).orElseThrow(() -> new ChannelNotInGuildException(textChannelId));
    }

    @Override
    public TextChannel getTextChannelFromServerNullable(Guild guild, Long textChannelId) {
        return getTextChannelFromServerOptional(guild, textChannelId).orElse(null);
    }

    @Override
    public Optional<TextChannel> getTextChannelFromServerOptional(Long serverId, Long textChannelId)  {
        Optional<Guild> guildOptional = getGuildByIdOptional(serverId);
        if(guildOptional.isPresent()) {
            Guild guild = guildOptional.get();
            return Optional.ofNullable(guild.getTextChannelById(textChannelId));
        }
        throw new GuildNotFoundException(serverId);
    }

    @Override
    public TextChannel getTextChannelFromServer(Long serverId, Long textChannelId) {
        return getTextChannelFromServerOptional(serverId, textChannelId).orElseThrow(() -> new ChannelNotInGuildException(textChannelId));
    }

    @Override
    public Optional<Guild> getGuildByIdOptional(Long serverId) {
        return Optional.ofNullable(instance.getGuildById(serverId));
    }

    @Override
    public Guild getGuildById(Long serverId) {
        Guild guildById = instance.getGuildById(serverId);
        if(guildById == null) {
            throw new GuildNotFoundException(serverId);
        }
        return guildById;
    }

    @Override
    public CompletableFuture<Guild> retrieveGuildById(Long serverId) {
        return null;
    }

    @Override
    public Member getBotInGuild(AServer server) {
        Guild guild = getGuildById(server.getId());
        return guild.getSelfMember();
    }

    @Override
    public CompletableFuture<User> getUserViaId(Long userId) {
        return instance.retrieveUserById(userId).submit();
    }
}
