package dev.sheldan.abstracto.core.startup;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.slash.SlashCommandService;
import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.listener.AsyncStartupListener;
import dev.sheldan.abstracto.core.command.SlashCommandListenerBean;
import dev.sheldan.abstracto.core.service.*;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class SlashCommandLoaderListener implements AsyncStartupListener {

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

    @Override
    public void execute() {
        List<Command> incomingSlashCommands = slashCommandListenerBean.getSlashCommands();
        JDA jda = botService.getInstance();
        List<Guild> onlineGuilds = jda.getGuilds();
        onlineGuilds.forEach(guild -> {
            log.info("Updating slash commands for guild {}.", guild.getIdLong());
            guild.retrieveCommands().queue(commands -> {
                        log.info("Loaded {} slash commands for guild {}.", commands.size(), guild.getIdLong());
                        List<Pair<List<CommandConfiguration>, SlashCommandData>> commandsToUpDate = new ArrayList<>();
                        incomingSlashCommands.forEach(command -> {
                            FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(command.getFeature());
                            if (!featureFlagService.isFeatureEnabled(feature, guild.getIdLong())) {
                                return;
                            }
                            if(!featureModeService.necessaryFeatureModesMet(command, guild.getIdLong())) {
                                return;
                            }
                            log.info("Updating slash command {} in guild {}.", command.getConfiguration().getName(), guild.getId());
                            slashCommandService.convertCommandConfigToCommandData(command.getConfiguration(), commandsToUpDate);
                        });
                        slashCommandService.updateGuildSlashCommand(guild, commandsToUpDate)
                                .thenAccept(commands1 -> log.info("Updated {} commands in guild {}.", commands1.size(), guild.getIdLong()));
                    },
                    throwable -> log.error("Failed to load commands for guild {}.", guild.getIdLong(), throwable));
        });
    }
}
