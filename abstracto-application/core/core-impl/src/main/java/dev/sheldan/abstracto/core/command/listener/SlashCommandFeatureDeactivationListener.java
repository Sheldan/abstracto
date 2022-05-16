package dev.sheldan.abstracto.core.command.listener;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.SlashCommandListenerBean;
import dev.sheldan.abstracto.core.command.model.database.ACommand;
import dev.sheldan.abstracto.core.command.model.database.ACommandInAServer;
import dev.sheldan.abstracto.core.command.service.management.CommandInServerManagementService;
import dev.sheldan.abstracto.core.command.service.management.CommandManagementService;
import dev.sheldan.abstracto.core.command.slash.SlashCommandService;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.entity.FeatureDeactivationListener;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.listener.FeatureDeactivationListenerModel;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SlashCommandFeatureDeactivationListener implements FeatureDeactivationListener {

    @Autowired
    private SlashCommandListenerBean slashCommandListenerBean;

    @Autowired
    private BotService botService;

    @Autowired
    private SlashCommandService slashCommandService;

    @Autowired
    private CommandInServerManagementService commandInServerManagementService;

    @Autowired
    private CommandManagementService commandManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public DefaultListenerResult execute(FeatureDeactivationListenerModel model) {
        List<Command> commandsToDelete = slashCommandListenerBean.getSlashCommands()
                .stream()
                .filter(command -> command.getFeature().getKey().equals(model.getFeatureName()))
                .collect(Collectors.toList());
        if(commandsToDelete.isEmpty()) {
            return DefaultListenerResult.IGNORED;
        }
        JDA jda = botService.getInstance();
        Guild guild = jda.getGuildById(model.getServerId());
        log.info("Updating slash commands for guild {}.", guild.getIdLong());
        guild.retrieveCommands().queue(commands -> {
            List<Long> existingCommands = commands
                    .stream()
                    .filter(command -> command.getType().equals(net.dv8tion.jda.api.interactions.commands.Command.Type.SLASH))
                    .map(ISnowflake::getIdLong)
                    .collect(Collectors.toList());

            log.info("Loaded {} slash commands for guild {}.", commands.size(), guild.getIdLong());
            Set<Long> commandIdsToDelete = new HashSet<>();
            List<Long> commandInServerIdsToUnset = new ArrayList<>();
            AServer server = serverManagementService.loadServer(guild.getIdLong());
            commandsToDelete.forEach(aCommandToDelete -> {
                ACommand aCommand = commandManagementService.findCommandByName(aCommandToDelete.getConfiguration().getName());
                ACommandInAServer commandInServer = commandInServerManagementService.getCommandForServer(aCommand, server);
                if(commandInServer.getSlashCommandId() != null && existingCommands.contains(commandInServer.getSlashCommandId())) {
                    commandIdsToDelete.add(commandInServer.getSlashCommandId());
                    commandInServerIdsToUnset.add(commandInServer.getSlashCommandId());
                }
            });
            slashCommandService.deleteGuildSlashCommands(guild, new ArrayList<>(commandIdsToDelete), commandInServerIdsToUnset).whenComplete((unused, throwable) -> {
                if(throwable != null) {
                    log.error("Failed to delete {} commands from guild {} for feature {}.", commandIdsToDelete.size(), guild.getIdLong(), model.getFeatureName(), throwable);
                } else {
                    log.info("Deleted {} commands for guild {} for feature {}.", commandIdsToDelete.size(), guild.getIdLong(), model.getFeatureName());
                }
            });
        },
        throwable -> log.error("Failed to load commands for guild {}.", guild.getIdLong(), throwable));
        return DefaultListenerResult.PROCESSED;
    }

}
