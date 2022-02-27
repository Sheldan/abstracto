package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.listener.async.MessageContextCommandListener;
import dev.sheldan.abstracto.core.listener.async.entity.FeatureActivationListener;
import dev.sheldan.abstracto.core.listener.sync.jda.MessageContextCommandListenerBean;
import dev.sheldan.abstracto.core.models.listener.FeatureActivationListenerModel;
import dev.sheldan.abstracto.core.service.ContextCommandService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.service.GuildService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MessageContextCommandFeatureActivationListener implements FeatureActivationListener {

    @Autowired
    private MessageContextCommandListenerBean listenerBean;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private ContextCommandService contextCommandService;

    @Autowired
    private GuildService guildService;

    @Override
    public DefaultListenerResult execute(FeatureActivationListenerModel model) {
        List<MessageContextCommandListener> listeners = listenerBean.getListenerList();
        if(listeners == null || listeners.isEmpty()) {
            return DefaultListenerResult.IGNORED;
        }
        Guild guild = guildService.getGuildById(model.getServerId());
        listeners.forEach(messageContextCommandListener -> {
            if(featureModeService.necessaryFeatureModesMet(messageContextCommandListener, model.getServerId())) {
                String contextCommandName = messageContextCommandListener.getConfig().getName();
                log.info("Adding message context command {} in guild {}.", contextCommandName, model.getServerId());
                contextCommandService.upsertGuildMessageContextCommand(guild, contextCommandName)
                        .thenAccept(command -> log.info("Created message context command {} in guild {} because feature {} was enabled.", contextCommandName, guild.getIdLong(), model.getFeatureName()));
            }
        });
        return DefaultListenerResult.PROCESSED;
    }
}
