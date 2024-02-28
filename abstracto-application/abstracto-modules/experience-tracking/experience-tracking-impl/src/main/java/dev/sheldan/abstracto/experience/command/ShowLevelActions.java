package dev.sheldan.abstracto.experience.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.experience.config.ExperienceFeatureDefinition;
import dev.sheldan.abstracto.experience.config.ExperienceFeatureMode;
import dev.sheldan.abstracto.experience.config.ExperienceSlashCommandNames;
import dev.sheldan.abstracto.experience.model.template.LevelActionsDisplay;
import dev.sheldan.abstracto.experience.service.LevelActionService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class ShowLevelActions extends AbstractConditionableCommand {

    private static final String COMMAND_NAME = "showLevelActions";
    private static final String TEMPLATE_KEY = "showLevelActions_response";

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private LevelActionService levelActionService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        LevelActionsDisplay levelActionsToDisplay = levelActionService.getLevelActionsToDisplay(event.getGuild());
        return interactionService.replyEmbed(TEMPLATE_KEY, levelActionsToDisplay, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(ExperienceSlashCommandNames.EXPERIENCE_CONFIG)
                .groupName("levelAction")
                .commandName("show")
                .build();

        return CommandConfiguration.builder()
                .name(COMMAND_NAME)
                .module(ExperienceModuleDefinition.EXPERIENCE)
                .templated(true)
                .slashCommandConfig(slashCommandConfig)
                .supportsEmbedException(true)
                .slashCommandOnly(true)
                .causesReaction(true)
                .help(helpInfo)
                .build();
    }

    @Override
    public List<FeatureMode> getFeatureModeLimitations() {
        return Arrays.asList(ExperienceFeatureMode.LEVEL_ACTION);
    }

    @Override
    public FeatureDefinition getFeature() {
        return ExperienceFeatureDefinition.EXPERIENCE;
    }
}
