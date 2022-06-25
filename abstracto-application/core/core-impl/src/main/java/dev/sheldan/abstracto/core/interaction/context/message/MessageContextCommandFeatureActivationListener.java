package dev.sheldan.abstracto.core.interaction.context.message;

import dev.sheldan.abstracto.core.interaction.MessageContextConfig;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.interaction.context.message.listener.MessageContextCommandListener;
import dev.sheldan.abstracto.core.listener.async.entity.FeatureActivationListener;
import dev.sheldan.abstracto.core.models.listener.FeatureActivationListenerModel;
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
        listeners = listeners
                .stream().filter(command -> command.getFeature().getKey().equals(model.getFeatureName()))
                .collect(Collectors.toList());
        Guild guild = guildService.getGuildById(model.getServerId());
        listeners.forEach(messageContextCommandListener -> {
            if(featureModeService.necessaryFeatureModesMet(messageContextCommandListener, model.getServerId())) {
                MessageContextConfig config = messageContextCommandListener.getConfig();
                String contextCommandName = contextCommandService.getCommandContextName(config, model.getServerId());
                log.info("Adding message context command {} in guild {}.", contextCommandName, model.getServerId());
                contextCommandService.upsertGuildMessageContextCommand(guild, contextCommandName, config)
                        .exceptionally(throwable -> {
                            log.error("Failed to greate message context command {} in guild {}.", contextCommandName, guild.getIdLong(), throwable);
                            return null;
                        });
            }
        });
        return DefaultListenerResult.PROCESSED;
    }
}
