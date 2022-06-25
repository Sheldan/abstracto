package dev.sheldan.abstracto.moderation.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.moderation.config.ModerationModuleDefinition;
import dev.sheldan.abstracto.moderation.config.ModerationSlashCommandNames;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.service.InfractionService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class EditInfraction extends AbstractConditionableCommand {

    private static final String EDIT_INFRACTION_COMMAND = "editInfraction";
    private static final String REASON_PARAMETER = "newReason";
    private static final String ID_PARAMETER = "id";
    private static final String EDIT_INFRACTION_RESPONSE = "editInfraction_response";

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InfractionService infractionService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Long infractionId = slashCommandParameterService.getCommandOption(ID_PARAMETER, event, Long.class, Integer.class).longValue();
        String newReason = slashCommandParameterService.getCommandOption(REASON_PARAMETER, event, String.class);
        return infractionService.editInfraction(infractionId, newReason, event.getGuild().getIdLong())
                .thenCompose(unused -> interactionService.replyEmbed(EDIT_INFRACTION_RESPONSE, event))
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();

        Parameter idParameter = Parameter
                .builder()
                .name(ID_PARAMETER)
                .type(Long.class)
                .templated(true)
                .build();
        parameters.add(idParameter);

        Parameter typeParameter = Parameter
                .builder()
                .name(REASON_PARAMETER)
                .type(String.class)
                .templated(true)
                .build();
        parameters.add(typeParameter);

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(ModerationSlashCommandNames.INFRACTIONS)
                .commandName("edit")
                .build();

        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();
        return CommandConfiguration.builder()
                .name(EDIT_INFRACTION_COMMAND)
                .module(ModerationModuleDefinition.MODERATION)
                .templated(true)
                .async(true)
                .causesReaction(false)
                .slashCommandConfig(slashCommandConfig)
                .supportsEmbedException(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.INFRACTIONS;
    }
}
