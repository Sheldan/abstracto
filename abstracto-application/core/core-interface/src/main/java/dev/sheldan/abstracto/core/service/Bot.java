package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.GuildChannelMember;
import dev.sheldan.abstracto.core.models.database.AEmote;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public interface Bot {
    void login() throws LoginException;
    JDA getInstance();
    GuildChannelMember getServerChannelUser(Long serverId, Long channelId, Long userId);
    Member getMemberInServer(Long serverId, Long memberId);
    CompletableFuture<Void> deleteMessage(Long serverId, Long channelId, Long messageId);
    Optional<Emote> getEmote(Long serverId, AEmote emote);
    Optional<TextChannel> getTextChannelFromServer(Guild serverId, Long textChannelId);
    Optional<TextChannel> getTextChannelFromServer(Long serverId, Long textChannelId);
    Optional<Guild> getGuildById(Long serverId);
    void shutdown();
}
