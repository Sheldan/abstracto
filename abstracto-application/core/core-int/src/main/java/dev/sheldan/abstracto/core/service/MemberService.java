package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.GuildChannelMember;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface MemberService {
    GuildChannelMember getServerChannelUser(Long serverId, Long channelId, Long userId);
    CompletableFuture<GuildChannelMember> getServerChannelUserAsync(Long serverId, Long channelId, Long userId);
    Member getMemberInServer(Long serverId, Long memberId);
    CompletableFuture<Member> getMemberInServerAsync(Long serverId, Long memberId);
    CompletableFuture<List<Member>> getMembersInServerAsync(Long serverId, List<Long> memberIds);
    CompletableFuture<Member> retrieveMemberInServer(ServerUser serverUser);
    CompletableFuture<User> retrieveUserById(Long userId);
    boolean isUserInGuild(AUserInAServer aUserInAServer);
    boolean isUserInGuild(Guild guild, AUserInAServer aUserInAServer);
    Member getMemberInServer(AUserInAServer aUserInAServer);
    Member getMemberInServer(ServerUser serverUser);
    CompletableFuture<Member> getMemberInServerAsync(ServerUser serverUser);
    CompletableFuture<Member> getMemberInServerAsync(AUserInAServer aUserInAServer);
    Member getMemberInServerAsync(AServer server, AUser member);
    CompletableFuture<Member> forceReloadMember(Member member);
    Member getBotInGuild(AServer server);
    CompletableFuture<User> getUserViaId(Long userId);
    CompletableFuture<Void> timeoutUser(Member member, Duration duration);
    CompletableFuture<Void> timeoutUser(Member member, Duration duration, String reason);
    CompletableFuture<Void> timeoutUserMaxDuration(Member member);
    CompletableFuture<Void> timeoutUser(Member member, Instant target);
    CompletableFuture<Void> timeoutUser(Member member, Instant target, String reason);
    CompletableFuture<Void> timeoutMember(Guild guild, ServerUser serverUser, Duration duration, String reason);
    CompletableFuture<Void> removeTimeout(Guild guild, ServerUser serverUser, String reason);
    CompletableFuture<Void> removeTimeout(Member member);
}
