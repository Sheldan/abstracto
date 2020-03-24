package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.ServerChannelUser;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;

@Service
public interface Bot {
    void login() throws LoginException;
    JDA getInstance();
    ServerChannelUser getServerChannelUser(Long serverId, Long channelId, Long userId);
    Member getMemberInServer(Long serverId, Long memberId);
    TextChannel getTextChannelFromServer(Long serverId, Long textChannelId);
    Guild getGuildById(Long serverId);
    void shutdown();
}
