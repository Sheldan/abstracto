package dev.sheldan.abstracto.core.interaction.context;

import dev.sheldan.abstracto.core.interaction.ApplicationCommandService;
import dev.sheldan.abstracto.core.interaction.ContextCommandConfig;
import dev.sheldan.abstracto.core.interaction.MessageContextConfig;
import dev.sheldan.abstracto.core.interaction.context.management.ContextCommandInServerManagementService;
import dev.sheldan.abstracto.core.interaction.context.management.ContextCommandManagementService;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.ContextCommand;
import dev.sheldan.abstracto.core.models.database.ContextCommandInServer;
import dev.sheldan.abstracto.core.models.listener.interaction.MessageContextInteractionModel;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
public class ContextCommandServiceBean implements ContextCommandService {

    @Autowired
    private ApplicationCommandService applicationCommandService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ContextCommandManagementService contextCommandManagementService;

    @Autowired
    private ContextCommandInServerManagementService contextCommandInServerManagementService;

    @Autowired
    private ContextCommandServiceBean self;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public CompletableFuture<Void> upsertGuildMessageContextCommand(Guild guild, String name, MessageContextConfig config) {
        return guild.upsertCommand(Commands.context(Command.Type.MESSAGE, name)).submit()
                .thenAccept(command -> self.storeCratedContextCommand(guild, command, config));
    }

    @Transactional
    public void storeCratedContextCommand(Guild guild, Command command, MessageContextConfig contextConfig) {
        ContextCommand contextCommand = contextCommandManagementService.findContextCommand(contextConfig.getName());
        AServer server = serverManagementService.loadServer(guild.getIdLong());
        contextCommandInServerManagementService.createOrUpdateContextCommandInServer(contextCommand, server, command.getIdLong());
    }

    @Override
    public CompletableFuture<Void> deleteGuildContextCommand(Guild guild, Long commandId) {
        return applicationCommandService.deleteGuildCommand(guild, commandId);
    }

    @Override
    public CompletableFuture<Void> deleteGuildContextCommandByName(Guild guild, MessageContextConfig contextConfig) {
        ContextCommand contextCommand = contextCommandManagementService.findContextCommand(contextConfig.getName());
        AServer server = serverManagementService.loadServer(guild.getIdLong());
        Optional<ContextCommandInServer> contextCommandInServer = contextCommandInServerManagementService.loadContextCommandInServer(contextCommand, server);
        return contextCommandInServer
                .map(contextCommandInServer1 -> deleteGuildContextCommand(guild, contextCommandInServer1.getContextCommandId()))
                .orElse(CompletableFuture.completedFuture(null))
                .thenAccept(unused -> self.resetContextCommandId(guild, contextConfig));
    }

    @Transactional
    public void resetContextCommandId(Guild guild, MessageContextConfig messageContextConfig) {
        ContextCommand contextCommand = contextCommandManagementService.findContextCommand(messageContextConfig.getName());
        AServer server = serverManagementService.loadServer(guild.getIdLong());
        Optional<ContextCommandInServer> contextCommandInServer = contextCommandInServerManagementService.loadContextCommandInServer(contextCommand, server);
        contextCommandInServer
                .ifPresent(contextCommandInServer1 -> contextCommandInServer1.setContextCommandId(null));
    }

    @Override
    public String getCommandContextName(MessageContextConfig contextConfig, Long guildId) {
        return contextConfig.getIsTemplated()
                ? templateService.renderSimpleTemplate(contextConfig.getTemplateKey(), guildId)
                : contextConfig.getName();
    }

    @Override
    public boolean matchesGuildContextName(MessageContextInteractionModel model, MessageContextConfig contextConfig, Long guidId) {
        return model.getEvent().isFromGuild()
                && model.getEvent().getCommandType().equals(Command.Type.MESSAGE)
                && model.getEvent().getName().equals(getCommandContextName(contextConfig, model.getServerId()));
    }

    @Override
    public void storeCreatedCommands(Command command, AServer server, List<ContextCommandConfig> contextCommands) {
        Optional<ContextCommandConfig> createdContextCommandOptional = contextCommands
                .stream()
                .filter(contextCommandConfig -> contextCommandConfig.getName().equals(command.getName()))
                .findFirst();
        if (createdContextCommandOptional.isPresent()) {
            ContextCommandConfig createdContextCommandConfig = createdContextCommandOptional.get();
            ContextCommand contextCommand = contextCommandManagementService.findContextCommand(createdContextCommandConfig.getMessageContextConfig().getName());
            contextCommandInServerManagementService.createOrUpdateContextCommandInServer(contextCommand, server, command.getIdLong());
        }
    }
}
