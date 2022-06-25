package dev.sheldan.abstracto.core.commands.config.profanity;

import dev.sheldan.abstracto.core.interaction.slash.CoreSlashCommandNames;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.commands.config.ConfigModuleDefinition;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.service.ProfanityService;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class AddProfanityRegex extends AbstractConditionableCommand {

    private static final String PROFANITY_GROUP_PARAMETER = "profanityGroup";
    private static final String PROFANITY_NAME_PARAMETER = "profanityName";
    private static final String REGEX_PARAMETER = "regex";
    private static final String REPLACEMENT_PARAMETER = "replacement";
    private static final String ADD_PROFANITY_REGEX_RESPONSE = "addProfanityRegex_response";
    private static final String ADD_PROFANITY_REGEX_COMMAND = "addProfanityRegex";

    @Autowired
    private ProfanityService profanityService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        String profanityGroupName = (String) parameters.get(0);
        String profanityName = (String) parameters.get(1);
        String regex = (String) parameters.get(2);
        Long serverId = commandContext.getGuild().getIdLong();
        if(parameters.size() < 4) {
            profanityService.createProfanityRegex(serverId, profanityGroupName, profanityName, regex);
        } else {
            String replacement = (String) parameters.get(3);
            profanityService.createProfanityRegex(serverId, profanityGroupName, profanityName, regex, replacement);
        }
        return CommandResult.fromSuccess();
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String profanityGroup = slashCommandParameterService.getCommandOption(PROFANITY_GROUP_PARAMETER, event, String.class);
        String profanityName = slashCommandParameterService.getCommandOption(PROFANITY_NAME_PARAMETER, event, String.class);
        String regex = slashCommandParameterService.getCommandOption(REGEX_PARAMETER, event, String.class);
        Long serverId = event.getGuild().getIdLong();
        if(slashCommandParameterService.hasCommandOption(REPLACEMENT_PARAMETER, event)) {
            String replacement = slashCommandParameterService.getCommandOption(REPLACEMENT_PARAMETER, event, String.class);
            profanityService.createProfanityRegex(serverId, profanityGroup, profanityName, regex, replacement);
        } else {
            profanityService.createProfanityRegex(serverId, profanityGroup, profanityName, regex);
        }
        return interactionService.replyEmbed(ADD_PROFANITY_REGEX_RESPONSE, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter profanityGroupParameter = Parameter
                .builder()
                .name(PROFANITY_GROUP_PARAMETER)
                .type(String.class)
                .templated(true)
                .build();
        Parameter nameParameter = Parameter
                .builder()
                .name(PROFANITY_NAME_PARAMETER)
                .type(String.class)
                .templated(true)
                .build();
        Parameter regexParameter = Parameter
                .builder()
                .name(REGEX_PARAMETER)
                .type(String.class)
                .templated(true)
                .build();
        Parameter replacement = Parameter
                .builder()
                .name(REPLACEMENT_PARAMETER)
                .type(String.class)
                .optional(true)
                .templated(true)
                .build();
        List<Parameter> parameters = Arrays.asList(profanityGroupParameter, nameParameter, regexParameter, replacement);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(CoreSlashCommandNames.PROFANITY)
                .commandName(ADD_PROFANITY_REGEX_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(ADD_PROFANITY_REGEX_COMMAND)
                .module(ConfigModuleDefinition.CONFIG)
                .parameters(parameters)
                .templated(true)
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
