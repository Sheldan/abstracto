package dev.sheldan.abstracto.core.commands.utility;

import dev.sheldan.abstracto.core.interaction.slash.CoreSlashCommandNames;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.*;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.config.validator.MaxStringLengthValidator;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.commands.config.ConfigModuleDefinition;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.service.management.EmoteManagementService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class SetEmote extends AbstractConditionableCommand {

    private static final String EMOTE_KEY_PARAMETER = "emoteKey";
    private static final String EMOTE_PARAMETER = "emote";
    private static final String RESPONSE_TEMPLATE = "setEmote_response";

    @Autowired
    private EmoteManagementService emoteManagementService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        String emoteKey = (String) parameters.get(0);
        AEmote emote = (AEmote) parameters.get(1);
        emoteManagementService.setEmoteToAEmote(emoteKey, emote, commandContext.getGuild().getIdLong());
        return CommandResult.fromSuccess();
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String emoteKey = slashCommandParameterService.getCommandOption(EMOTE_KEY_PARAMETER, event, String.class);
        String emote = slashCommandParameterService.getCommandOption(EMOTE_PARAMETER, event, String.class);
        AEmote aEmote = slashCommandParameterService.loadAEmoteFromString(emote, event.getGuild());
        emoteManagementService.setEmoteToAEmote(emoteKey, aEmote, event.getGuild().getIdLong());
        return interactionService.replyEmbed(RESPONSE_TEMPLATE, new Object(), event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<ParameterValidator> emoteKeyValidators = Arrays.asList(MaxStringLengthValidator.max(255L));
        Parameter emoteKey = Parameter
                .builder()
                .name(EMOTE_KEY_PARAMETER)
                .validators(emoteKeyValidators)
                .type(String.class)
                .templated(true)
                .build();
        Parameter emote = Parameter
                .builder()
                .name(EMOTE_PARAMETER)
                .type(AEmote.class)
                .templated(true)
                .build();
        List<Parameter> parameters = Arrays.asList(emoteKey, emote);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();
        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(CoreSlashCommandNames.CONFIG)
                .commandName("setEmote")
                .build();
        return CommandConfiguration.builder()
                .name("setEmote")
                .module(ConfigModuleDefinition.CONFIG)
                .parameters(parameters)
                .supportsEmbedException(true)
                .help(helpInfo)
                .slashCommandConfig(slashCommandConfig)
                .templated(true)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return CoreFeatureDefinition.CORE_FEATURE;
    }
}
