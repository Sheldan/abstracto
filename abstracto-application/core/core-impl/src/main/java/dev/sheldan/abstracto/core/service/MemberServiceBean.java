package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.exception.ChannelNotInGuildException;
import dev.sheldan.abstracto.core.exception.GuildNotFoundException;
import dev.sheldan.abstracto.core.models.GuildChannelMember;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
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
        Guild guild = guildService.getGuildById(serverId);
        Optional<TextChannel> textChannelOptional = channelService.getTextChannelFromServerOptional(guild, channelId);
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
        log.debug("Trying to retrieve member {}, channel {} in server {} async.", userId, channelId, serverId);
        CompletableFuture<Member> memberFuture = getMemberInServerAsync(serverId, userId);

        Guild guild = guildService.getGuildById(serverId);
        TextChannel textChannel = channelService.getTextChannelFromServer(guild, channelId);
        return memberFuture.thenApply(member ->
                GuildChannelMember.builder().guild(guild).textChannel(textChannel).member(member).build()
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
    public CompletableFuture<Member> getMemberInServerAsync(AUserInAServer aUserInAServer) {
        return getMemberInServerAsync(aUserInAServer.getServerReference().getId(), aUserInAServer.getUserReference().getId());
    }

    @Override
    public Member getMemberInServer(AServer server, AUser member) {
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
}
