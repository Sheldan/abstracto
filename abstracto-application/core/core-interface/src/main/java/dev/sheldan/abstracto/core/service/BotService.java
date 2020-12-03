package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.GuildChannelMember;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public interface BotService {
    void login() throws LoginException;
    JDA getInstance();
    GuildChannelMember getServerChannelUser(Long serverId, Long channelId, Long userId);
    CompletableFuture<GuildChannelMember> getServerChannelUserAsync(Long serverId, Long channelId, Long userId);
    Member getMemberInServer(Long serverId, Long memberId);
    CompletableFuture<Member> getMemberInServerAsync(Long serverId, Long memberId);
    boolean isUserInGuild(AUserInAServer aUserInAServer);
    boolean isUserInGuild(Guild guild, AUserInAServer aUserInAServer);
    Member getMemberInServer(AUserInAServer aUserInAServer);
    CompletableFuture<Member> getMemberInServerAsync(AUserInAServer aUserInAServer);
    Member getMemberInServer(AServer server, AUser member);
    CompletableFuture<Void> deleteMessage(Long serverId, Long channelId, Long messageId);
    CompletableFuture<Void> deleteMessage(Long channelId, Long messageId);
    CompletableFuture<Member> forceReloadMember(Member member);
    Optional<Emote> getEmote(Long serverId, AEmote emote);
    Optional<Emote> getEmote(AEmote emote);
    Optional<TextChannel> getTextChannelFromServerOptional(Guild serverId, Long textChannelId);
    TextChannel getTextChannelFromServer(Guild guild, Long textChannelId);
    TextChannel getTextChannelFromServerNullable(Guild guild, Long textChannelId);
    Optional<TextChannel> getTextChannelFromServerOptional(Long serverId, Long textChannelId);
    TextChannel getTextChannelFromServer(Long serverId, Long textChannelId);
    Optional<Guild> getGuildByIdOptional(Long serverId);
    Guild getGuildById(Long serverId);
    Member getBotInGuild(AServer server);
}
