package dev.sheldan.abstracto.utility.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
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
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.utility.config.UtilityFeatureDefinition;
import dev.sheldan.abstracto.utility.config.UtilitySlashCommandNames;
import dev.sheldan.abstracto.utility.model.ShowAvatarModel;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class ShowAvatar extends AbstractConditionableCommand {

    public static final String SHOW_AVATAR_RESPONSE_TEMPLATE = "showAvatar_response";
    private static final String MEMBER_PARAMETER = "member";
    private static final String SHOW_AVATAR_COMMAND = "showAvatar";

    @Autowired
    private ChannelService channelService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Member memberToShow = parameters.size() == 1 ? (Member) parameters.get(0) : commandContext.getUserInitiatedContext().getMember();
        if(!memberToShow.getGuild().equals(commandContext.getGuild())) {
            throw new EntityGuildMismatchException();
        }
        ShowAvatarModel model = ShowAvatarModel
                .builder()
                .memberInfo(memberToShow)
                .build();
        log.info("Showing avatar for member {} towards user {} in channel {} in server {}.",
                memberToShow.getId(), commandContext.getAuthor().getId(), commandContext.getChannel().getId(), commandContext.getGuild().getId());
        return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInTextChannelList(SHOW_AVATAR_RESPONSE_TEMPLATE, model, commandContext.getChannel()))
                .thenApply(aVoid -> CommandResult.fromIgnored());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Member memberToShow;
        if(slashCommandParameterService.hasCommandOption(MEMBER_PARAMETER, event)) {
            memberToShow = slashCommandParameterService.getCommandOption(MEMBER_PARAMETER, event, Member.class);
        } else {
            memberToShow = event.getMember();
        }
        ShowAvatarModel model = ShowAvatarModel
                .builder()
                .memberInfo(memberToShow)
                .build();
        return interactionService.replyEmbed(SHOW_AVATAR_RESPONSE_TEMPLATE, model, event.getInteraction())
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Parameter memberParameter = Parameter
                .builder()
                .type(Member.class)
                .name(MEMBER_PARAMETER)
                .templated(true)
                .optional(true)
                .build();
        parameters.add(memberParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(UtilitySlashCommandNames.UTILITY)
                .commandName(SHOW_AVATAR_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(SHOW_AVATAR_COMMAND)
                .slashCommandConfig(slashCommandConfig)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .async(true)
                .supportsEmbedException(true)
                .causesReaction(false)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return UtilityFeatureDefinition.UTILITY;
    }
}
