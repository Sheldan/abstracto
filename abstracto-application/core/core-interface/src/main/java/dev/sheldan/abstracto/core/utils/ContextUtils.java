package dev.sheldan.abstracto.core.utils;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.UserManagementService;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.GuildChannelMember;
import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.BotService;
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
    private BotService botService;

    public <T extends UserInitiatedServerContext> UserInitiatedServerContext fromMessage(CachedMessage message, Class<T> clazz) {
        Method m = null;
        GuildChannelMember guildChannelMember = botService.getServerChannelUser(message.getServerId(), message.getChannelId(), message.getAuthorId());
        try {
            m = clazz.getMethod("builder");
            UserInitiatedServerContext.UserInitiatedServerContextBuilder<?, ?> builder = (UserInitiatedServerContext.UserInitiatedServerContextBuilder) m.invoke(null, null);
            AUserInAServer aUserInAServer = userManagementService.loadUser(message.getServerId(), message.getAuthorId());
            return builder
                    .member(guildChannelMember.getMember())
                    .guild(guildChannelMember.getGuild())
                    .messageChannel(guildChannelMember.getTextChannel())
                    .channel(channelManagementService.loadChannel(message.getChannelId()))
                    .server(aUserInAServer.getServerReference())
                    .aUserInAServer(aUserInAServer)
                    .user(aUserInAServer.getUserReference())
                    .build();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error("Failed to execute builder method", e);
        }
        throw new AbstractoRunTimeException("Failed to create model from message");
    }
}
