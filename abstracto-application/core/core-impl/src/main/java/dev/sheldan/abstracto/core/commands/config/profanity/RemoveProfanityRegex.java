package dev.sheldan.abstracto.core.commands.config.profanity;

import dev.sheldan.abstracto.core.interaction.slash.CoreSlashCommandNames;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.commands.config.ConfigModuleDefinition;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.service.ProfanityService;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class RemoveProfanityRegex extends AbstractConditionableCommand {

    private static final String REMOVE_PROFANITY_REGEX_COMMAND = "removeProfanityRegex";
    private static final String PROFANITY_NAME_PARAMETER = "profanityName";
    private static final String PROFANITY_GROUP_NAME_PARAMETER = "profanityGroupName";
    private static final String REMOVE_PROFANITY_REGEX_RESPONSE = "removeProfanityRegex_response";

    @Autowired
    private ProfanityService profanityService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String profanityGroup = slashCommandParameterService.getCommandOption(PROFANITY_GROUP_NAME_PARAMETER, event, String.class);
        profanityService.deleteProfanityGroup(event.getGuild().getIdLong(), profanityGroup);
        return interactionService.replyEmbed(REMOVE_PROFANITY_REGEX_RESPONSE, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter profanityGroupParameter = Parameter
                .builder()
                .name(PROFANITY_GROUP_NAME_PARAMETER)
                .type(String.class)
                .templated(true)
                .build();
        Parameter profanityNameParameter = Parameter
                .builder()
                .name(PROFANITY_NAME_PARAMETER)
                .type(String.class)
                .templated(true)
                .build();
        List<Parameter> parameters = Arrays.asList(profanityGroupParameter, profanityNameParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .defaultPrivilege(SlashCommandPrivilegeLevels.ADMIN)
                .rootCommandName(CoreSlashCommandNames.PROFANITY)
                .commandName(REMOVE_PROFANITY_REGEX_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(REMOVE_PROFANITY_REGEX_COMMAND)
                .module(ConfigModuleDefinition.CONFIG)
                .parameters(parameters)
                .templated(true)
                .slashCommandOnly(true)
                .slashCommandConfig(slashCommandConfig)
                .supportsEmbedException(true)
                .help(helpInfo)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return CoreFeatureDefinition.CORE_FEATURE;
    }
}
