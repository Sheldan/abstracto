package dev.sheldan.abstracto.core.interaction;

import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.model.database.ACommand;
import dev.sheldan.abstracto.core.command.model.database.ACommandInAServer;
import dev.sheldan.abstracto.core.command.service.management.CommandInServerManagementService;
import dev.sheldan.abstracto.core.command.service.management.CommandManagementService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class InteractionCommandServiceBean implements InteractionCommandService {

    @Autowired
    private InteractionCommandServiceBean self;

    @Autowired
    private CommandManagementService commandManagementService;

    @Autowired
    private CommandInServerManagementService commandInServerManagementService;

    @Override
    public CompletableFuture<List<Command>> updateGuildCommands(Guild guild, List<Pair<List<CommandConfiguration>, SlashCommandData>> slashCommands, List<ContextCommandConfig> contextCommands) {
        List<CommandData> slashCommandData = slashCommands
                .stream()
                .map(Pair::getSecond)
                .collect(Collectors.toList());

        List<CommandData> contextCommandData = contextCommands
                .stream()
                .map(s -> Commands.context(s.getType(), s.getName()))
                .collect(Collectors.toList());

        List<CommandData> allCommands = Stream.concat(slashCommandData.stream(), contextCommandData.stream())
                .collect(Collectors.toList());
        return guild.updateCommands().addCommands(allCommands).submit().thenApply(createdCommands -> {
            self.storeCreatedCommands(guild, slashCommands, contextCommands, createdCommands);
            return createdCommands;
        });
    }

    @Transactional
    public void storeCreatedCommands(Guild guild, List<Pair<List<CommandConfiguration>, SlashCommandData>> slashCommands, List<ContextCommandConfig> contextCommands, List<Command> createdCommands) {
        slashCommands.forEach(commandConfigurationSlashCommandDataPair -> {
            SlashCommandData slashCommandData = commandConfigurationSlashCommandDataPair.getSecond();
            commandConfigurationSlashCommandDataPair.getFirst().forEach(commandConfiguration -> {
                ACommand aCommand = commandManagementService.findCommandByName(commandConfiguration.getName());
                ACommandInAServer commandInServer = commandInServerManagementService.getCommandForServer(aCommand, guild.getIdLong());
                Command createdCommand = createdCommands.stream().filter(command -> doesCommandMatch(slashCommandData, command)).findFirst().orElse(null);
                if(createdCommand != null) {
                    commandInServer.setSlashCommandId(createdCommand.getIdLong());
                }
            });
        });
    }

    private boolean doesCommandMatch(SlashCommandData commandConfig, Command command) {
        return commandConfig.getName().equals(command.getName());
    }
}
