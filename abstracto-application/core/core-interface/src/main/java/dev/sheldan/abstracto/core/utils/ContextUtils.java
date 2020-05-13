package dev.sheldan.abstracto.core.utils;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.exception.ChannelNotFoundException;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
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
import java.util.Optional;

@Component
@Slf4j
public class ContextUtils {

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private BotService botService;

    public <T extends UserInitiatedServerContext> UserInitiatedServerContext fromMessage(CachedMessage message, Class<T> clazz) {
        Method m = null;
        GuildChannelMember guildChannelMember = botService.getServerChannelUser(message.getServerId(), message.getChannelId(), message.getAuthorId());
        try {
            m = clazz.getMethod("builder");
            UserInitiatedServerContext.UserInitiatedServerContextBuilder<?, ?> builder = (UserInitiatedServerContext.UserInitiatedServerContextBuilder) m.invoke(null, null);
            AUserInAServer aUserInAServer = userInServerManagementService.loadUser(message.getServerId(), message.getAuthorId());
            Optional<AChannel> channelOptional = channelManagementService.loadChannel(message.getChannelId());
            AChannel channel = channelOptional.orElseThrow(() -> new ChannelNotFoundException(message.getChannelId(), message.getServerId()));
            return builder
                    .member(guildChannelMember.getMember())
                    .guild(guildChannelMember.getGuild())
                    .messageChannel(guildChannelMember.getTextChannel())
                    .channel(channel)
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
