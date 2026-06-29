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
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandAutoCompleteService;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.UserService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.core.utils.SnowflakeUtils;
import dev.sheldan.abstracto.modmail.config.ModMailFeatureDefinition;
import dev.sheldan.abstracto.modmail.config.ModMailSlashCommandNames;
import dev.sheldan.abstracto.modmail.exception.ModMailThreadClosedException;
import dev.sheldan.abstracto.modmail.model.database.ModMailThread;
import dev.sheldan.abstracto.modmail.model.database.ModMailThreadState;
import dev.sheldan.abstracto.modmail.model.database.QuickReply;
import dev.sheldan.abstracto.modmail.service.ModMailThreadService;
import dev.sheldan.abstracto.modmail.service.QuickReplyService;
import dev.sheldan.abstracto.modmail.service.management.ModMailThreadManagementService;
import dev.sheldan.abstracto.modmail.service.management.QuickReplyManagementService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class QuickReplyCommand extends AbstractConditionableCommand {

    private static final String QUICK_REPLY_COMMAND = "quickReply";
    private static final String QUICK_REPLY_NAME_PARAMETER = "name";
    private static final String QUICK_REPLY_ANONYMOUS_PARAMETER = "anonymous";
    private static final String QUICK_REPLY_RESPONSE_TEMPLATE_KEY = "quickReply_response";
    private static final String QUICK_REPLY_NO_QUICK_REPLY_FOUND_TEMPLATE_KEY = "quickReply_no_quick_reply_response";

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private QuickReplyService quickReplyService;

    @Autowired
    private QuickReplyManagementService quickReplyManagementService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private SlashCommandAutoCompleteService slashCommandAutoCompleteService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private ModMailThreadService modMailThreadService;

    @Autowired
    private ModMailThreadManagementService modMailThreadManagementService;

    @Autowired
    private UserService userService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String name = slashCommandParameterService.getCommandOption(QUICK_REPLY_NAME_PARAMETER, event, String.class);
        Optional<QuickReply> quickReplyOptional = quickReplyService.getQuickReply(name, event.getGuild());
        if(quickReplyOptional.isEmpty()) {
            return interactionService.replyEmbed(QUICK_REPLY_NO_QUICK_REPLY_FOUND_TEMPLATE_KEY, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
        }
        ModMailThread modMailThread = modMailThreadManagementService.getByChannelId(event.getChannel().getIdLong());
        if(ModMailThreadState.CLOSED.equals(modMailThread.getState()) || ModMailThreadState.CLOSING.equals(modMailThread.getState())) {
            throw new ModMailThreadClosedException();
        }
        Long threadId = modMailThread.getId();
        QuickReply quickReply = quickReplyOptional.get();
        Boolean anonymousOverride = null;
        if(slashCommandParameterService.hasCommandOption(QUICK_REPLY_ANONYMOUS_PARAMETER, event)) {
            anonymousOverride = slashCommandParameterService.getCommandOption(QUICK_REPLY_ANONYMOUS_PARAMETER, event, Boolean.class);
        }
        boolean anonymous;
        if(anonymousOverride != null) {
            anonymous = anonymousOverride;
        } else {
            anonymous = quickReply.getAnonymous();
        }
        Long snowFlake = SnowflakeUtils.createSnowFlake();
        Long targetUserId = modMailThread.getUser().getUserReference().getId();
        event.deferReply(true).queue();
        return
            userService.retrieveUserForId(targetUserId).thenCompose(user ->
            modMailThreadService.relayMessageToDm(threadId, quickReply.getAdditionalMessage(), snowFlake, anonymous, user,
                event.getGuild(), event.getMember())
        ).thenCompose(unused -> FutureUtils.toSingleFutureGeneric(interactionService.sendMessageToInteraction(QUICK_REPLY_RESPONSE_TEMPLATE_KEY, new Object(), event.getHook())))
        .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public List<String> performAutoComplete(CommandAutoCompleteInteractionEvent event) {
        if(slashCommandAutoCompleteService.matchesParameter(event.getFocusedOption(), QUICK_REPLY_NAME_PARAMETER)) {
            String input = event.getFocusedOption().getValue();
            AServer server = serverManagementService.loadServer(event.getGuild());
            return quickReplyManagementService.getQuickRepliesContaining(input, server)
                .stream().map(quickReply -> quickReply.getName().toLowerCase())
                .toList();
        }
        return new ArrayList<>();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModMailFeatureDefinition.MOD_MAIL;
    }

    @Override
    public CommandConfiguration getConfiguration() {
        HelpInfo helpInfo = HelpInfo
            .builder()
            .templated(true)
            .build();

        Parameter replyContentParameter = Parameter
            .builder()
            .name(QUICK_REPLY_NAME_PARAMETER)
            .templated(true)
            .supportsAutoComplete(true)
            .type(String.class)
            .build();

        Parameter replyAnonymousparameter = Parameter
            .builder()
            .name(QUICK_REPLY_ANONYMOUS_PARAMETER)
            .templated(true)
            .optional(true)
            .type(Boolean.class)
            .build();

        List<Parameter> parameters = Arrays.asList(replyContentParameter, replyAnonymousparameter);

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
            .builder()
            .enabled(true)
            .rootCommandName(ModMailSlashCommandNames.MODMAIL)
            .commandName("quickReply")
            .build();

        return CommandConfiguration.builder()
            .name(QUICK_REPLY_COMMAND)
            .module(UtilityModuleDefinition.UTILITY)
            .templated(true)
            .async(true)
            .slashCommandOnly(true)
            .slashCommandConfig(slashCommandConfig)
            .causesReaction(true)
            .parameters(parameters)
            .supportsEmbedException(true)
            .help(helpInfo)
            .build();
    }

}
