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
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.moderation.config.ModerationModuleDefinition;
import dev.sheldan.abstracto.moderation.config.ModerationSlashCommandNames;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.service.MuteService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class UnMute extends AbstractConditionableCommand {

    private static final String UN_MUTE_COMMAND = "unMute";
    private static final String USER_PARAMETER = "user";
    private static final String UN_MUTE_RESPONSE = "unMute_response";

    @Autowired
    private MuteService muteService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Member member = (Member) parameters.get(0);
        if(!member.getGuild().equals(commandContext.getGuild())) {
            throw new EntityGuildMismatchException();
        }
        ServerUser userToUnmute = ServerUser.fromMember(member);
        ServerUser unMutingMember = ServerUser.fromMember(commandContext.getAuthor());
        return muteService.unMuteUser(userToUnmute, unMutingMember, commandContext.getGuild()).thenApply(aVoid ->
            CommandResult.fromSuccess()
        );
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Member targetMember = slashCommandParameterService.getCommandOption(USER_PARAMETER, event, Member.class);
        if(!targetMember.getGuild().equals(event.getGuild())) {
            throw new EntityGuildMismatchException();
        }
        ServerUser userToUnmute = ServerUser.fromMember(targetMember);
        ServerUser unMutingMember = ServerUser.fromMember(event.getMember());
        return muteService.unMuteUser(userToUnmute, unMutingMember, event.getGuild())
                .thenCompose(unused -> interactionService.replyEmbed(UN_MUTE_RESPONSE, event))
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
        List<Parameter> parameters = Arrays.asList(userParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(ModerationSlashCommandNames.MUTE)
                .commandName("remove")
                .build();

        return CommandConfiguration.builder()
                .name(UN_MUTE_COMMAND)
                .module(ModerationModuleDefinition.MODERATION)
                .templated(true)
                .async(true)
                .slashCommandConfig(slashCommandConfig)
                .supportsEmbedException(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.MUTING;
    }
}
