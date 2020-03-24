package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.ServerChannelUser;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;
import java.util.EnumSet;

@Service
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
        TextChannel textChannelById = getTextChannelFromServer(serverId, channelId);
        Guild guildById  = getGuildById(serverId);
        if(textChannelById != null) {
            Member member = guildById.getMemberById(userId);
            return ServerChannelUser.builder().guild(guildById).textChannel(textChannelById).member(member).build();
        }
        throw new RuntimeException(String.format("Member %s or text channel %s not found in guild %s", userId, channelId, serverId));
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
    public TextChannel getTextChannelFromServer(Long serverId, Long textChannelId) {
        Guild guild = getGuildById(serverId);
        TextChannel textChannelById = guild.getTextChannelById(textChannelId);
        if(textChannelById != null) {
            return textChannelById;
        } else {
            throw new RuntimeException(String.format("Text channel %s in guild %s not found", textChannelId, serverId));
        }
    }

    @Override
    public Guild getGuildById(Long serverId) {
        Guild guildById = instance.getGuildById(serverId);
        if(guildById != null) {
            return guildById;
        } else {
            throw new RuntimeException(String.format("Guild %s not found", serverId));
        }
    }

    @Override
    public void shutdown() {

    }
}
