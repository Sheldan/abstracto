package dev.sheldan.abstracto.core.startup;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.listener.AsyncStartupListener;
import dev.sheldan.abstracto.core.listener.async.MessageContextCommandListener;
import dev.sheldan.abstracto.core.listener.sync.jda.MessageContextCommandListenerBean;
import dev.sheldan.abstracto.core.service.*;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MessageContextCommandListenerLoader implements AsyncStartupListener {

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private BotService botService;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private MessageContextCommandListenerBean listenerBean;

    @Autowired
    private ContextCommandService contextCommandService;

    @Override
    public void execute() {
        List<MessageContextCommandListener> contextListeners = listenerBean.getListenerList();
        if(contextListeners == null || contextListeners.isEmpty()) {
            return;
        }
        JDA jda = botService.getInstance();
        List<Guild> onlineGuilds = jda.getGuilds();
        onlineGuilds.forEach(guild -> {
            log.info("Updating commands for guild {}.", guild.getIdLong());
            guild.retrieveCommands().queue(commands -> {
                Map<String, Long> existingCommands = commands
                        .stream()
                        .filter(command -> command.getType().equals(Command.Type.MESSAGE))
                        .collect(Collectors.toMap(Command::getName, ISnowflake::getIdLong));

                log.info("Loaded {} commands for guild {}.", commands.size(), guild.getIdLong());
                contextListeners.forEach(listener -> {
                    FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(listener.getFeature());
                    if (!featureFlagService.isFeatureEnabled(feature, guild.getIdLong())) {
                        return;
                    }
                    if(!featureModeService.necessaryFeatureModesMet(listener, guild.getIdLong())) {
                        return;
                    }
                    log.info("Updating message context command {} in guild {}.", listener.getConfig().getName(), guild.getId());
                    if(existingCommands.containsKey(listener.getConfig().getName())) {
                        existingCommands.remove(listener.getConfig().getName());
                        contextCommandService.upsertGuildMessageContextCommand(guild, listener.getConfig().getName())
                                .thenAccept(command -> log.info("Updated message context command {} in guild {}.", listener.getConfig().getName(), guild.getId()));
                    }
                });
                log.info("Deleting {} message context commands in guild {}.", existingCommands.values().size(), guild.getIdLong());
                existingCommands.forEach((commandName, commandId) ->
                        contextCommandService.deleteGuildContextCommand(guild, commandId)
                            .thenAccept(unused -> log.info("Deleted message context command {} with id {} in guild {}.", commandName, commandId, guild.getIdLong()))
                            .exceptionally(throwable -> {
                                log.warn("Failed to delete message context command {} with id {} in guild {}.", commandName, commandId, guild.getIdLong());
                                return null;
                            }));
            },
            throwable -> log.error("Failed to load commands for guild {}.", guild.getIdLong(), throwable));
        });

    }
}
