package dev.sheldan.abstracto.core.utils;

import dev.sheldan.abstracto.core.command.service.UserService;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.dto.UserInServerDto;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.GuildChannelMember;
import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import dev.sheldan.abstracto.core.service.Bot;
import dev.sheldan.abstracto.core.service.ChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Component
@Slf4j
public class ContextUtils {

    @Autowired
    private ChannelService channelService;

    @Autowired
    private UserService userService;

    @Autowired
    private Bot bot;

    public <T extends UserInitiatedServerContext> UserInitiatedServerContext fromMessage(CachedMessage message, Class<T> clazz) {
        Method m = null;
        GuildChannelMember guildChannelMember = bot.getServerChannelUser(message.getServerId(), message.getChannelId(), message.getAuthorId());
        try {
            m = clazz.getMethod("builder");
            UserInitiatedServerContext.UserInitiatedServerContextBuilder<?, ?> builder = (UserInitiatedServerContext.UserInitiatedServerContextBuilder) m.invoke(null, null);
            UserInServerDto aUserInAServer = userService.loadUser(message.getServerId(), message.getAuthorId());
            return builder
                    .member(guildChannelMember.getMember())
                    .guild(guildChannelMember.getGuild())
                    .messageChannel(guildChannelMember.getTextChannel())
                    .channel(channelService.loadChannel(message.getChannelId()))
                    .server(aUserInAServer.getServer())
                    .aUserInAServer(aUserInAServer)
                    .user(aUserInAServer.getUser())
                    .build();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error("Failed to execute builder method", e);
        }
        throw new AbstractoRunTimeException("Failed to create model from message");
    }
}
