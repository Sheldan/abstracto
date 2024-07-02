package dev.sheldan.abstracto.core.interaction;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.interaction.context.ContextCommandService;
import dev.sheldan.abstracto.core.interaction.context.message.MessageContextCommandListenerBean;
import dev.sheldan.abstracto.core.interaction.context.message.listener.MessageContextCommandListener;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandListenerBean;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandService;
import dev.sheldan.abstracto.core.listener.AsyncStartupListener;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CommandLoaderListener implements AsyncStartupListener {

   @Autowired
   private SlashCommandListenerBean slashCommandListenerBean;

   @Autowired
   private SlashCommandService slashCommandService;

   @Autowired
   private BotService botService;

   @Autowired
   private FeatureConfigService featureConfigService;

   @Autowired
   private FeatureFlagService featureFlagService;

   @Autowired
   private FeatureModeService featureModeService;

    @Autowired
    private MessageContextCommandListenerBean listenerBean;

    @Autowired
    private ContextCommandService contextCommandService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private CommandLoaderListener self;

    @Autowired
    private InteractionCommandService interactionCommandService;

    @Override
    public void execute() {
        List<Command> incomingSlashCommands = slashCommandListenerBean.getSlashCommands();

        List<MessageContextCommandListener> contextListeners = listenerBean.getListenerList();

        JDA jda = botService.getInstance();
        List<Guild> onlineGuilds = jda.getGuilds();
        onlineGuilds.forEach(guild -> {
            log.info("Updating slash commands for guild {}.", guild.getIdLong());
            List<Pair<List<CommandConfiguration>, SlashCommandData>> slashCommandsToUpdate = new ArrayList<>();
            incomingSlashCommands.forEach(command -> {
                FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(command.getFeature());
                if (!featureFlagService.isFeatureEnabled(feature, guild.getIdLong())) {
                    return;
                }
                if(!featureModeService.necessaryFeatureModesMet(command, guild.getIdLong())) {
                    return;
                }
                log.info("Updating slash command {} in guild {}.", command.getConfiguration().getName(), guild.getId());
                slashCommandService.convertCommandConfigToCommandData(command.getConfiguration(), slashCommandsToUpdate, guild.getIdLong(), false);
            });

            log.info("Updating context commands for guild {}.", guild.getIdLong());
            List<ContextCommandConfig> contextCommandsToUpdate = new ArrayList<>();
            contextListeners.forEach(listener -> {
                FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(listener.getFeature());
                if (!featureFlagService.isFeatureEnabled(feature, guild.getIdLong())) {
                    return;
                }
                if(!featureModeService.necessaryFeatureModesMet(listener, guild.getIdLong())) {
                    return;
                }
                String commandName = contextCommandService.getCommandContextName(listener.getConfig(), guild.getIdLong());
                ContextCommandConfig contextCommandToCreate = ContextCommandConfig
                        .builder()
                        .name(commandName)
                        .messageContextConfig(listener.getConfig())
                        .type(net.dv8tion.jda.api.interactions.commands.Command.Type.MESSAGE)
                        .build();
                contextCommandsToUpdate.add(contextCommandToCreate);
                log.info("Updating message context command {} in guild {}.", commandName, guild.getId());
            });
            interactionCommandService.updateGuildCommands(guild, slashCommandsToUpdate, contextCommandsToUpdate).thenAccept(commands -> {
                try {
                    self.storeCreatedCommands(commands, guild, slashCommandsToUpdate, contextCommandsToUpdate);
                } catch (Exception throwable) {
                    log.error("Failed to store created commands in guild {}.", guild.getIdLong(), throwable);
                }
            }).exceptionally(throwable -> {
                log.error("Failed to update guild commands in guild {}.", guild.getIdLong(), throwable);
                return null;
            });
        });
        List<Pair<List<CommandConfiguration>, SlashCommandData>> userCommandsToUpdate = new ArrayList<>();
        incomingSlashCommands.forEach(command -> {
            slashCommandService.convertCommandConfigToCommandData(command.getConfiguration(), userCommandsToUpdate, null, true);
        });
        List<CommandData> userCommands = userCommandsToUpdate
                .stream()
                .map(Pair::getSecond)
                .collect(Collectors.toList());
        jda.updateCommands().addCommands(userCommands).queue();
    }

    @Transactional
    public void storeCreatedCommands(List<net.dv8tion.jda.api.interactions.commands.Command> createdCommands, Guild guild,
                                     List<Pair<List<CommandConfiguration>, SlashCommandData>> slashCommands, List<ContextCommandConfig> contextCommands) {
        AServer server = serverManagementService.loadServer(guild.getIdLong());
        createdCommands.forEach(command -> {
            if(command.getType().equals(net.dv8tion.jda.api.interactions.commands.Command.Type.SLASH)) {
                slashCommandService.storeCreatedSlashCommands(guild, slashCommands, createdCommands);
            } else if(command.getType().equals(net.dv8tion.jda.api.interactions.commands.Command.Type.MESSAGE)) {
                contextCommandService.storeCreatedCommands(command, server, contextCommands);
            }
        });
    }
}
