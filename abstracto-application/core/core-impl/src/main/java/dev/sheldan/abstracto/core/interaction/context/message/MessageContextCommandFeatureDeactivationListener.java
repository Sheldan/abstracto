package dev.sheldan.abstracto.core.interaction.context.message;

import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.interaction.context.message.listener.MessageContextCommandListener;
import dev.sheldan.abstracto.core.listener.async.entity.FeatureDeactivationListener;
import dev.sheldan.abstracto.core.models.listener.FeatureDeactivationListenerModel;
import dev.sheldan.abstracto.core.interaction.context.ContextCommandService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.service.GuildService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MessageContextCommandFeatureDeactivationListener implements FeatureDeactivationListener {

    @Autowired
    private MessageContextCommandListenerBean listenerBean;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private ContextCommandService contextCommandService;

    @Autowired
    private GuildService guildService;

    @Override
    public DefaultListenerResult execute(FeatureDeactivationListenerModel model) {
        List<MessageContextCommandListener> listeners = listenerBean.getListenerList();
        if(listeners == null || listeners.isEmpty()) {
            return DefaultListenerResult.IGNORED;
        }
        listeners = listeners
                .stream().filter(command -> command.getFeature().getKey().equals(model.getFeatureName()))
                .collect(Collectors.toList());
        Guild guild = guildService.getGuildById(model.getServerId());
        listeners.forEach(messageContextCommandListener -> {
            if(featureModeService.necessaryFeatureModesMet(messageContextCommandListener, model.getServerId())) {
                String contextCommandName = contextCommandService.getCommandContextName(messageContextCommandListener.getConfig(), guild.getIdLong());
                log.info("Adding message context command {} in guild {}.", contextCommandName, model.getServerId());
                contextCommandService.deleteGuildContextCommandByName(guild, messageContextCommandListener.getConfig())
                        .thenAccept(unused -> log.info("Deleted command {} because feature {} was disabled in guild {}.", contextCommandName, model.getFeatureName(), guild.getIdLong()))
                        .exceptionally(throwable -> {
                            log.error("Failed to delete message context command in guild {}.", guild.getIdLong(), throwable);
                            return null;
                        });
            }
        });
        return DefaultListenerResult.PROCESSED;
    }
}
