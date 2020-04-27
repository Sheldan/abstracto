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
    Member getMemberInServer(Long serverId, Long memberId);
    boolean isUserInGuild(AUserInAServer aUserInAServer);
    boolean isUserInGuild(Guild guild, AUserInAServer aUserInAServer);
    Member getMemberInServer(AUserInAServer aUserInAServer);
    Member getMemberInServer(AServer server, AUser member);
    CompletableFuture<Void> deleteMessage(Long serverId, Long channelId, Long messageId);
    Optional<Emote> getEmote(Long serverId, AEmote emote);
    Optional<TextChannel> getTextChannelFromServer(Guild serverId, Long textChannelId);
    Optional<TextChannel> getTextChannelFromServer(Long serverId, Long textChannelId);
    Optional<Guild> getGuildById(Long serverId);
    Guild getGuildByIdNullable(Long serverId);
    void shutdown();
}
