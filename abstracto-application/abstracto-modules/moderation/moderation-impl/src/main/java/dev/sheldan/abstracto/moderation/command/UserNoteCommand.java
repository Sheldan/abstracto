package dev.sheldan.abstracto.moderation.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.EntityGuildMismatchException;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.moderation.config.ModerationModuleDefinition;
import dev.sheldan.abstracto.moderation.config.ModerationSlashCommandNames;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.service.management.UserNoteManagementService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class UserNoteCommand extends AbstractConditionableCommand {

    private static final String USER_PARAMETER = "user";
    private static final String TEXT_PARAMETER = "text";
    private static final String USER_NOTE_COMMAND = "userNote";
    private static final String USER_NOTE_RESPONSE = "userNote_response";

    @Autowired
    private UserNoteManagementService userNoteManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Member member = (Member) parameters.get(0);
        if(!member.getGuild().equals(commandContext.getGuild())) {
            throw new EntityGuildMismatchException();
        }
        String text = (String) parameters.get(1);
        AUserInAServer userInAServer = userInServerManagementService.loadOrCreateUser(member);
        userNoteManagementService.createUserNote(userInAServer, text);
        return CommandResult.fromSuccess();
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Member member = slashCommandParameterService.getCommandOption(USER_PARAMETER, event, Member.class);
        if(!member.getGuild().equals(event.getGuild())) {
            throw new EntityGuildMismatchException();
        }
        String text = slashCommandParameterService.getCommandOption(TEXT_PARAMETER, event, String.class);
        AUserInAServer userInAServer = userInServerManagementService.loadOrCreateUser(member);
        userNoteManagementService.createUserNote(userInAServer, text);
        return interactionService.replyEmbed(USER_NOTE_RESPONSE, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter userParameter = Parameter
                .builder()
                .name(USER_PARAMETER)
                .type(Member.class)
                .templated(true)
                .build();
        Parameter textParameter = Parameter
                .builder()
                .name(TEXT_PARAMETER)
                .type(String.class)
                .templated(true)
                .remainder(true)
                .build();
        List<Parameter> parameters = Arrays.asList(userParameter, textParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(ModerationSlashCommandNames.USER_NOTES)
                .commandName("create")
                .build();

        return CommandConfiguration.builder()
                .name(USER_NOTE_COMMAND)
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
