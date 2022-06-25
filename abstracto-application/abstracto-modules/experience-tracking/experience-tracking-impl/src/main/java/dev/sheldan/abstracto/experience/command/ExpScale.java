package dev.sheldan.abstracto.experience.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.experience.config.ExperienceFeatureDefinition;
import dev.sheldan.abstracto.experience.config.ExperienceFeatureConfig;
import dev.sheldan.abstracto.experience.config.ExperienceSlashCommandNames;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;


/**
 * Command used to change the experience multiplier on the server.
 */
@Component
@Slf4j
public class ExpScale extends AbstractConditionableCommand {

    private static final String EXP_SCALE_COMMAND = "expScale";
    private static final String EXP_SCALE_RESPONSE = "expScale_response";
    private static final String SCALE_PARAMETER = "scale";

    @Autowired
    private ConfigService configService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        Double scale = (Double) commandContext.getParameters().getParameters().get(0);
        Long guildId = commandContext.getGuild().getIdLong();
        configService.setDoubleValue(ExperienceFeatureConfig.EXP_MULTIPLIER_KEY, guildId, scale);
        log.info("Setting experience scale to {} for {}", scale, guildId);
        return CommandResult.fromSuccess();
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Double newScale = slashCommandParameterService.getCommandOption(SCALE_PARAMETER, event, Double.class);
        configService.setDoubleValue(ExperienceFeatureConfig.EXP_MULTIPLIER_KEY, event.getGuild().getIdLong(), newScale);
        return interactionService.replyEmbed(EXP_SCALE_RESPONSE, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Parameter newScale = Parameter
                .builder()
                .name(SCALE_PARAMETER)
                .templated(true)
                .type(Double.class)
                .build();
        parameters.add(newScale);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(ExperienceSlashCommandNames.EXPERIENCE_CONFIG)
                .commandName(EXP_SCALE_COMMAND)
                .build();


        return CommandConfiguration.builder()
                .name(EXP_SCALE_COMMAND)
                .module(ExperienceModuleDefinition.EXPERIENCE)
                .causesReaction(true)
                .slashCommandConfig(slashCommandConfig)
                .templated(true)
                .supportsEmbedException(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ExperienceFeatureDefinition.EXPERIENCE;
    }
}
