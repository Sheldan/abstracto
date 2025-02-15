package dev.sheldan.abstracto.utility.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.*;
import dev.sheldan.abstracto.core.command.handler.parameter.CombinedParameter;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.utility.config.UtilityFeatureDefinition;
import dev.sheldan.abstracto.utility.config.UtilitySlashCommandNames;
import dev.sheldan.abstracto.utility.model.ShowAvatarModel;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static dev.sheldan.abstracto.core.command.config.Parameter.ADDITIONAL_TYPES_KEY;

@Component
@Slf4j
public class ShowAvatar extends AbstractConditionableCommand {

    public static final String SHOW_AVATAR_RESPONSE_TEMPLATE = "showAvatar_response";
    private static final String MEMBER_PARAMETER = "member";
    private static final String SHOW_AVATAR_COMMAND = "showAvatar";

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Value("${abstracto.feature.avatar.imagesize}")
    private Integer imageSize;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String avatarUrl;
        Member targetMember;
        if(slashCommandParameterService.hasCommandOption(MEMBER_PARAMETER, event)) {
            targetMember = slashCommandParameterService.getCommandOption(MEMBER_PARAMETER, event, Member.class);
        } else {
            targetMember = event.getMember();
        }
        if(targetMember == null) {
            User targetUser;
            if(slashCommandParameterService.hasCommandOption(MEMBER_PARAMETER, event)) {
                targetUser = slashCommandParameterService.getCommandOption(MEMBER_PARAMETER, event, User.class);
            } else {
                targetUser = event.getUser();
            }
            avatarUrl = targetUser.getEffectiveAvatar().getUrl(imageSize);
        } else {
            avatarUrl = targetMember.getEffectiveAvatar().getUrl(imageSize);
        }
        ShowAvatarModel model = ShowAvatarModel
                .builder()
                .avatarUrl(avatarUrl)
                .build();
        return interactionService.replyEmbed(SHOW_AVATAR_RESPONSE_TEMPLATE, model, event.getInteraction())
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Map<String, Object> parameterAlternatives = new HashMap<>();
        parameterAlternatives.put(ADDITIONAL_TYPES_KEY, List.of(
                CombinedParameterEntry.messageParameter(Message.class),
                CombinedParameterEntry.parameter(Member.class),
                CombinedParameterEntry.parameter(User.class)));
        Parameter memberParameter = Parameter
                .builder()
                .name(MEMBER_PARAMETER)
                .type(CombinedParameter.class)
                .additionalInfo(parameterAlternatives)
                .templated(true)
                .useStrictParameters(true)
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
                .userInstallable(true)
                .userCommandConfig(UserCommandConfig.guildOnly())
                .rootCommandName(UtilitySlashCommandNames.UTILITY)
                .commandName(SHOW_AVATAR_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(SHOW_AVATAR_COMMAND)
                .slashCommandConfig(slashCommandConfig)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .slashCommandOnly(true)
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
