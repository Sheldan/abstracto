package dev.sheldan.abstracto.stickyroles.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.service.UserService;
import dev.sheldan.abstracto.stickyroles.config.StickyRolesFeatureDefinition;
import dev.sheldan.abstracto.stickyroles.config.StickyRolesSlashCommandNames;
import dev.sheldan.abstracto.stickyroles.service.StickyRoleService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class ToggleStickinessManagement extends AbstractConditionableCommand {

    private static final String COMMAND_NAME = "toggleStickinessManagement";
    private static final String RESPONSE_TEMPLATE = "toggleStickinessManagement_response";

    private static final String MEMBER_PARAMETER_NAME = "member";
    private static final String STICKY_PARAMETER_NAME = "sticky";

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private StickyRoleService stickyRoleService;

    @Autowired
    private UserService userService;

    @Autowired
    private ToggleStickinessManagement self;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Boolean newState = slashCommandParameterService.getCommandOption(STICKY_PARAMETER_NAME, event, Boolean.class);
        if(slashCommandParameterService.hasCommandOptionWithFullType(MEMBER_PARAMETER_NAME, event, OptionType.USER)) {
            Member targetMember = slashCommandParameterService.getCommandOption(MEMBER_PARAMETER_NAME, event, User.class, Member.class);
            stickyRoleService.setStickiness(targetMember, newState);
            return interactionService.replyEmbed(RESPONSE_TEMPLATE, event)
                    .thenApply(interactionHook -> CommandResult.fromSuccess());
        } else {
            String userIdStr = slashCommandParameterService.getCommandOption(MEMBER_PARAMETER_NAME, event, User.class, String.class);
            Long userId = Long.parseLong(userIdStr);
            return userService.retrieveUserForId(userId).thenCompose(user -> {
                self.callService(event, user, newState);
                return interactionService.replyEmbed(RESPONSE_TEMPLATE, event);
            }).thenApply(interactionHook -> CommandResult.fromSuccess());
        }
    }


    @Transactional
    public void callService(SlashCommandInteractionEvent event, User user, Boolean newState) {
        stickyRoleService.setStickiness(user, event.getGuild(), newState);
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter memberParameter = Parameter
                .builder()
                .name(MEMBER_PARAMETER_NAME)
                .type(User.class)
                .optional(false)
                .templated(true)
                .build();


        Parameter stateParameter = Parameter
                .builder()
                .name(STICKY_PARAMETER_NAME)
                .type(Boolean.class)
                .optional(false)
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(StickyRolesSlashCommandNames.STICKY_ROLES)
                .commandName("manage")
                .build();

        List<Parameter> parameters = Arrays.asList(memberParameter, stateParameter);

        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();
        return CommandConfiguration.builder()
                .name(COMMAND_NAME)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .parameters(parameters)
                .slashCommandConfig(slashCommandConfig)
                .async(true)
                .slashCommandOnly(true)
                .supportsEmbedException(true)
                .help(helpInfo)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return StickyRolesFeatureDefinition.STICKY_ROLES;
    }
}
