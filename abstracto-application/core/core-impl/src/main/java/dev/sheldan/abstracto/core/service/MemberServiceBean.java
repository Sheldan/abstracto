package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.exception.GuildNotFoundException;
import dev.sheldan.abstracto.core.models.GuildChannelMember;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class MemberServiceBean implements MemberService {

    @Autowired
    private GuildService guildService;

    @Autowired
    private BotService botService;

    @Autowired
    private ChannelService channelService;

    @Override
    public GuildChannelMember getServerChannelUser(Long serverId, Long channelId, Long userId)  {
        log.debug("Trying to retrieve member {}, channel {} in server {} from cache.", userId, channelId, serverId);
        GuildChannel guildChannel = channelService.getGuildChannelFromServer(serverId, channelId);
        Guild guild = guildService.getGuildById(serverId);
        Member member = guild.getMemberById(userId);
        return GuildChannelMember.builder().guild(guild).textChannel(guildChannel).member(member).build();
    }

    @Override
    public CompletableFuture<GuildChannelMember> getServerChannelUserAsync(Long serverId, Long channelId, Long userId) {
        log.debug("Trying to retrieve member {}, channel {} in server {} async.", userId, channelId, serverId);
        CompletableFuture<Member> memberFuture = getMemberInServerAsync(serverId, userId);

        Guild guild = guildService.getGuildById(serverId);
        GuildMessageChannel messageChannel = channelService.getMessageChannelFromServer(guild, channelId);
        return memberFuture.thenApply(member ->
                GuildChannelMember.builder().guild(guild).textChannel(messageChannel).member(member).build()
        );
    }

    @Override
    public Member getMemberInServer(Long serverId, Long memberId) {
        log.debug("Retrieving member {} in server {} from cache.", memberId, serverId);
        Guild guildById = guildService.getGuildById(serverId);
        if(guildById != null) {
            return guildById.getMemberById(memberId);
        } else {
            throw new GuildNotFoundException(serverId);
        }
    }

    @Override
    public CompletableFuture<Member> getMemberInServerAsync(Long serverId, Long memberId) {
        log.debug("Retrieving member {} in server {} from cache.", memberId, serverId);
        Guild guildById = guildService.getGuildById(serverId);
        if(guildById != null) {
            return guildById.retrieveMemberById(memberId).submit();
        } else {
            throw new GuildNotFoundException(serverId);
        }
    }

    @Override
    public CompletableFuture<List<Member>> getMembersInServerAsync(Long serverId, List<Long> memberIds) {
        log.debug("Retrieving member {} in server {} from cache.", memberIds, serverId);
        Guild guildById = guildService.getGuildById(serverId);
        CompletableFuture<List<Member>> future = new CompletableFuture<>();
        if(guildById != null) {
            guildById.retrieveMembersByIds(memberIds).onSuccess(future::complete).onError(future::completeExceptionally);
        } else {
            throw new GuildNotFoundException(serverId);
        }
        return future;
    }

    @Override
    public CompletableFuture<Member> retrieveMemberInServer(ServerUser serverUser) {
        return getMemberInServerAsync(serverUser.getServerId(), serverUser.getUserId());
    }

    @Override
    public CompletableFuture<User> retrieveUserById(Long userId) {
        return botService.getInstance().retrieveUserById(userId).submit();
    }

    @Override
    public boolean isUserInGuild(AUserInAServer aUserInAServer) {
        Guild guildById = guildService.getGuildById(aUserInAServer.getServerReference().getId());
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
    public Member getMemberInServer(ServerUser serverUser) {
        return getMemberInServer(serverUser.getServerId(), serverUser.getUserId());
    }

    @Override
    public CompletableFuture<Member> getMemberInServerAsync(ServerUser serverUser) {
        return getMemberInServerAsync(serverUser.getServerId(), serverUser.getUserId());
    }

    @Override
    public CompletableFuture<Member> getMemberInServerAsync(AUserInAServer aUserInAServer) {
        return getMemberInServerAsync(aUserInAServer.getServerReference().getId(), aUserInAServer.getUserReference().getId());
    }

    @Override
    public Member getMemberInServerAsync(AServer server, AUser member) {
        return getMemberInServer(server.getId(), member.getId());
    }

    @Override
    public CompletableFuture<Member> forceReloadMember(Member member) {
        return member.getGuild().retrieveMember(member.getUser()).submit();
    }

    @Override
    public Member getBotInGuild(AServer server) {
        Guild guild = guildService.getGuildById(server.getId());
        return guild.getSelfMember();
    }

    @Override
    public CompletableFuture<User> getUserViaId(Long userId) {
        return botService.getInstance().retrieveUserById(userId).submit();
    }

    @Override
    public CompletableFuture<Void> timeoutUser(Member member, Duration duration) {

        return timeoutUser(member, duration, null);
    }

    @Override
    public CompletableFuture<Void> timeoutUser(Member member, Duration duration, String reason) {
        return timeoutMember(member.getGuild(), ServerUser.fromMember(member), duration, reason);
    }

    @Override
    public CompletableFuture<Void> timeoutUserMaxDuration(Member member) {
        return timeoutUser(member, Duration.ofDays(Member.MAX_TIME_OUT_LENGTH));
    }

    @Override
    public CompletableFuture<Void> timeoutUser(Member member, Instant target) {
        return timeoutUser(member, target, null);
    }

    @Override
    public CompletableFuture<Void> timeoutUser(Member member, Instant target, String reason) {
        Duration muteDuration = Duration.between(Instant.now(), target);
        return timeoutUser(member, muteDuration, reason);
    }

    @Override
    public CompletableFuture<Void> timeoutMember(Guild guild, ServerUser serverUser, Duration duration, String reason) {
        return guild.timeoutFor(UserSnowflake.fromId(serverUser.getUserId()), duration).reason(reason).submit();
    }

    @Override
    public CompletableFuture<Void> removeTimeout(Guild guild, ServerUser serverUser, String reason) {
        log.info("Removing timeout for user {} in guild {}.", serverUser.getUserId(), guild.getIdLong());
        return guild.removeTimeout(UserSnowflake.fromId(serverUser.getUserId())).reason(reason).submit();
    }

    @Override
    public CompletableFuture<Void> removeTimeout(Member member) {
        return removeTimeout(member.getGuild(), ServerUser.fromMember(member), null);
    }
}
