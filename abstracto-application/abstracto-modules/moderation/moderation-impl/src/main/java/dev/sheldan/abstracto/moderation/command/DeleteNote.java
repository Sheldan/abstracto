package dev.sheldan.abstracto.moderation.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.*;
import dev.sheldan.abstracto.core.command.config.validator.MinIntegerValueValidator;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.moderation.config.ModerationModuleDefinition;
import dev.sheldan.abstracto.moderation.config.ModerationSlashCommandNames;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.model.database.UserNote;
import dev.sheldan.abstracto.moderation.service.management.UserNoteManagementService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class DeleteNote extends AbstractConditionableCommand {

    private static final String DELETE_NOTE_COMMAND = "deleteNote";
    private static final String DELETE_NOTE_RESPONSE = "deleteNote_response";
    private static final String ID_PARAMETER = "id";

    @Autowired
    private UserNoteManagementService userNoteManagementService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        Long id = (Long) commandContext.getParameters().getParameters().get(0);
        UserNote existingNote = userNoteManagementService.loadNote(commandContext.getGuild().getIdLong(), id);
        userNoteManagementService.deleteNote(existingNote);
        return CommandResult.fromSuccess();
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Long userNoteId = slashCommandParameterService.getCommandOption(ID_PARAMETER, event, Integer.class).longValue();
        UserNote existingNote = userNoteManagementService.loadNote(event.getGuild().getIdLong(), userNoteId);
        userNoteManagementService.deleteNote(existingNote);
        return interactionService.replyEmbed(DELETE_NOTE_RESPONSE, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        List<ParameterValidator> userNoteIdValidator = Arrays.asList(MinIntegerValueValidator.min(1L));
        Parameter idParameter = Parameter
                .builder()
                .name(ID_PARAMETER)
                .validators(userNoteIdValidator)
                .type(Long.class)
                .templated(true)
                .build();
        parameters.add(idParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(ModerationSlashCommandNames.USER_NOTES)
                .defaultPrivilege(SlashCommandPrivilegeLevels.ADMIN)
                .commandName("delete")
                .build();

        return CommandConfiguration.builder()
                .name(DELETE_NOTE_COMMAND)
                .slashCommandConfig(slashCommandConfig)
                .module(ModerationModuleDefinition.MODERATION)
                .templated(true)
                .supportsEmbedException(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.USER_NOTES;
    }
}
