package dev.sheldan.abstracto.core.interaction.slash.listener;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandListenerBean;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandService;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.entity.FeatureActivationListener;
import dev.sheldan.abstracto.core.models.listener.FeatureActivationListenerModel;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SlashCommandFeatureActivationListener implements FeatureActivationListener {

    @Autowired
    private SlashCommandListenerBean slashCommandListenerBean;

    @Autowired
    private BotService botService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private SlashCommandService slashCommandService;

    @Override
    public DefaultListenerResult execute(FeatureActivationListenerModel model) {
        List<Command> incomingSlashCommands = slashCommandListenerBean.getSlashCommands()
                .stream()
                .filter(command -> command.getFeature().getKey().equals(model.getFeatureName()))
                .collect(Collectors.toList());
        if(incomingSlashCommands.isEmpty()) {
            return DefaultListenerResult.IGNORED;
        }
        JDA jda = botService.getInstance();
        Guild guild = jda.getGuildById(model.getServerId());
        log.info("Updating slash commands for guild {}.", guild.getIdLong());
        List<Pair<List<CommandConfiguration>, SlashCommandData>> commandsToUpDate = new ArrayList<>();
        incomingSlashCommands.forEach(command -> {
            if(!featureModeService.necessaryFeatureModesMet(command, guild.getIdLong())) {
                return;
            }
            log.info("Updating slash command {} in guild {}.", command.getConfiguration().getName(), guild.getId());
            slashCommandService.convertCommandConfigToCommandData(command.getConfiguration(), commandsToUpDate, model.getServerId());
        });
        slashCommandService.addGuildSlashCommands(guild, commandsToUpDate)
                .thenAccept(commands1 -> log.info("Updating {} slash commands in guild {}.", commandsToUpDate.size(), guild.getIdLong()));

        return DefaultListenerResult.PROCESSED;
    }
}
