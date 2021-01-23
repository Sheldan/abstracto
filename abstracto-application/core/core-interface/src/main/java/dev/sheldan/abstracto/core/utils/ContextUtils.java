package dev.sheldan.abstracto.core.utils;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.GuildChannelMember;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
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
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private BotService botService;

    public <T extends UserInitiatedServerContext> UserInitiatedServerContext fromMessage(CachedMessage message, Class<T> clazz) {
        Method m = null;
        GuildChannelMember guildChannelMember = botService.getServerChannelUser(message.getServerId(), message.getChannelId(), message.getAuthor().getAuthorId());
        try {
            m = clazz.getMethod("builder");
            UserInitiatedServerContext.UserInitiatedServerContextBuilder<?, ?> builder = (UserInitiatedServerContext.UserInitiatedServerContextBuilder) m.invoke(null, null);
            return builder
                    .member(guildChannelMember.getMember())
                    .guild(guildChannelMember.getGuild())
                    .messageChannel(guildChannelMember.getTextChannel())
                    .build();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error("Failed to execute builder method", e);
        }
        throw new AbstractoRunTimeException("Failed to create UserInitiatedServerContext from message");
    }
}
