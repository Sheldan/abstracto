package dev.sheldan.abstracto.core.utils;

import dev.sheldan.abstracto.core.management.ChannelManagementService;
import dev.sheldan.abstracto.core.management.ServerManagementService;
import dev.sheldan.abstracto.core.management.UserManagementService;
import dev.sheldan.abstracto.core.models.CachedMessage;
import dev.sheldan.abstracto.core.models.ServerChannelUser;
import dev.sheldan.abstracto.core.models.UserInitiatedServerContext;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.Bot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Component
@Slf4j
public class ContextUtils {

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private Bot bot;

    public <T extends UserInitiatedServerContext> UserInitiatedServerContext fromMessage(CachedMessage message, Class<T> clazz) {
        Method m = null;
        ServerChannelUser serverChannelUser = bot.getServerChannelUser(message.getServerId(), message.getChannelId(), message.getAuthorId());
        try {
            m = clazz.getMethod("builder");
            UserInitiatedServerContext.UserInitiatedServerContextBuilder<?, ?> builder = (UserInitiatedServerContext.UserInitiatedServerContextBuilder) m.invoke(null, null);
            AUserInAServer aUserInAServer = userManagementService.loadUser(message.getServerId(), message.getAuthorId());
            return builder
                    .member(serverChannelUser.getMember())
                    .guild(serverChannelUser.getGuild())
                    .textChannel(serverChannelUser.getTextChannel())
                    .channel(channelManagementService.loadChannel(message.getChannelId()))
                    .server(serverManagementService.loadOrCreate(message.getServerId()))
                    .aUserInAServer(aUserInAServer)
                    .user(aUserInAServer.getUserReference())
                    .build();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error("Failed to execute builder method", e);
        }
        throw new RuntimeException("Failed to create model from message");
    }
}
