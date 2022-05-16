package dev.sheldan.abstracto.moderation.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.*;
import dev.sheldan.abstracto.core.command.config.validator.MinIntegerValueValidator;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.moderation.config.ModerationModuleDefinition;
import dev.sheldan.abstracto.moderation.config.ModerationSlashCommandNames;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.model.database.Warning;
import dev.sheldan.abstracto.moderation.service.management.WarnManagementService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class DeleteWarning extends AbstractConditionableCommand {

    private static final String DELETE_WARNING_COMMAND = "deleteWarning";
    private static final String DELETE_WARNING_RESPONSE = "deleteWarning_response";
    private static final String WARN_ID_PARAMETER = "warnId";

    @Autowired
    private WarnManagementService warnManagementService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        Long warnId = (Long) commandContext.getParameters().getParameters().get(0);
        Warning warning = warnManagementService.findById(warnId, commandContext.getGuild().getIdLong());
        warnManagementService.deleteWarning(warning);
        return CommandResult.fromSuccess();
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Long warnId = slashCommandParameterService.getCommandOption(WARN_ID_PARAMETER, event, Long.class, Integer.class).longValue();
        Warning warning = warnManagementService.findById(warnId, event.getGuild().getIdLong());
        warnManagementService.deleteWarning(warning);
        return interactionService.replyEmbed(DELETE_WARNING_RESPONSE, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {

        List<ParameterValidator> warnIdValidator = Arrays.asList(MinIntegerValueValidator.min(1L));
        Parameter warnIdParameter = Parameter
                .builder()
                .name(WARN_ID_PARAMETER)
                .validators(warnIdValidator)
                .templated(true)
                .type(Long.class)
                .build();
        List<Parameter> parameters = Arrays.asList(warnIdParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(ModerationSlashCommandNames.WARNINGS)
                .commandName("delete")
                .build();

        List<String> aliases = Arrays.asList("delWarn");
        return CommandConfiguration.builder()
                .name(DELETE_WARNING_COMMAND)
                .slashCommandConfig(slashCommandConfig)
                .module(ModerationModuleDefinition.MODERATION)
                .templated(true)
                .supportsEmbedException(true)
                .aliases(aliases)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.WARNING;
    }
}
