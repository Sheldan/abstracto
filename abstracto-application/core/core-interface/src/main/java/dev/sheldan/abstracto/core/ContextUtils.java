package dev.sheldan.abstracto.core;

import dev.sheldan.abstracto.core.management.ChannelManagementService;
import dev.sheldan.abstracto.core.management.ServerManagementService;
import dev.sheldan.abstracto.core.management.UserManagementService;
import dev.sheldan.abstracto.core.models.UserInitiatedServerContext;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
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

    public <T extends UserInitiatedServerContext> UserInitiatedServerContext fromMessage(Message message, Class<T> clazz) {
        Method m = null;
        try {
            m = clazz.getMethod("builder");
            UserInitiatedServerContext.UserInitiatedServerContextBuilder<?, ?> builder = (UserInitiatedServerContext.UserInitiatedServerContextBuilder) m.invoke(null, null);
            AUserInAServer aUserInAServer = userManagementService.loadUser(message.getMember());
            return builder
                    .member(message.getMember())
                    .guild(message.getGuild())
                    .textChannel(message.getTextChannel())
                    .channel(channelManagementService.loadChannel(message.getTextChannel().getIdLong()))
                    .server(serverManagementService.loadServer(message.getGuild().getIdLong()))
                    .aUserInAServer(aUserInAServer)
                    .user(aUserInAServer.getUserReference())
                    .build();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error("Failed to execute builder method", e);
        }
        throw new RuntimeException("Failed to create model from message");
    }
}
