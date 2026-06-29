package dev.sheldan.abstracto.modmail.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.modmail.config.ModMailFeatureDefinition;
import dev.sheldan.abstracto.modmail.config.ModMailSlashCommandNames;
import dev.sheldan.abstracto.modmail.service.QuickReplyService;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class QuickReplyCreate extends AbstractConditionableCommand {

    private static final String CREATE_QUICK_REPLY_COMMAND = "createQuickReply";
    private static final String QUICK_REPLY_NAME_PARAMETER = "name";
    private static final String QUICK_REPLY_CONTENT_PARAMETER = "response";
    private static final String QUICK_REPLY_ANONYMOUS_PARAMETER = "anonymous";
    private static final String CREATE_QUICK_REPLY_RESPONSE_TEMPLATE_KEY = "createQuickReply_response";

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private QuickReplyService quickReplyService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String name = slashCommandParameterService.getCommandOption(QUICK_REPLY_NAME_PARAMETER, event, String.class);
        String content = slashCommandParameterService.getCommandOption(QUICK_REPLY_CONTENT_PARAMETER, event, String.class);
        boolean anonymous;
        if(slashCommandParameterService.hasCommandOption(QUICK_REPLY_ANONYMOUS_PARAMETER, event)) {
            anonymous = slashCommandParameterService.getCommandOption(QUICK_REPLY_ANONYMOUS_PARAMETER, event, Boolean.class);
        } else {
            anonymous = false;
        }
        quickReplyService.createQuickReply(name, content, event.getMember(), anonymous);
        return interactionService.replyEmbed(CREATE_QUICK_REPLY_RESPONSE_TEMPLATE_KEY, event)
            .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter commandNameParameter = Parameter
            .builder()
            .name(QUICK_REPLY_NAME_PARAMETER)
            .templated(true)
            .type(String.class)
            .build();

        Parameter replyContentParameter = Parameter
            .builder()
            .name(QUICK_REPLY_CONTENT_PARAMETER)
            .templated(true)
            .type(String.class)
            .build();

        Parameter replyAnonymousparameter = Parameter
            .builder()
            .name(QUICK_REPLY_ANONYMOUS_PARAMETER)
            .templated(true)
            .optional(true)
            .type(Boolean.class)
            .build();

        List<Parameter> parameters = Arrays.asList(commandNameParameter, replyContentParameter, replyAnonymousparameter);
        HelpInfo helpInfo = HelpInfo
            .builder()
            .templated(true)
            .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
            .builder()
            .enabled(true)
            .defaultPrivilege(SlashCommandPrivilegeLevels.ADMIN)
            .rootCommandName(ModMailSlashCommandNames.MODMAIL)
            .commandName("createQuickReply")
            .build();

        return CommandConfiguration.builder()
            .name(CREATE_QUICK_REPLY_COMMAND)
            .module(UtilityModuleDefinition.UTILITY)
            .templated(true)
            .async(true)
            .slashCommandOnly(true)
            .slashCommandConfig(slashCommandConfig)
            .causesReaction(true)
            .supportsEmbedException(true)
            .parameters(parameters)
            .help(helpInfo)
            .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModMailFeatureDefinition.MOD_MAIL;
    }
}
