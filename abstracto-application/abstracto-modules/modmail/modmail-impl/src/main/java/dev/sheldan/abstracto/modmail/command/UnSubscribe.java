package dev.sheldan.abstracto.modmail.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.condition.CommandCondition;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.modmail.condition.ModMailContextCondition;
import dev.sheldan.abstracto.modmail.config.ModMailFeatureDefinition;
import dev.sheldan.abstracto.modmail.config.ModMailSlashCommandNames;
import dev.sheldan.abstracto.modmail.model.database.ModMailThread;
import dev.sheldan.abstracto.modmail.service.ModMailSubscriptionService;
import dev.sheldan.abstracto.modmail.service.management.ModMailThreadManagementService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;


/**
 * This command un-subscribes the member executing this command from the {@link ModMailThread} in which the command was executed in.
 * In case the member is not subscribed an error message is displayed.
 */
@Component
public class UnSubscribe extends AbstractConditionableCommand {

    private static final String UN_SUBSCRIBE_COMMAND = "unSubscribe";
    private static final String UN_SUBSCRIBE_RESPONSE = "unSubscribe_response";

    @Autowired
    private ModMailContextCondition requiresModMailCondition;

    @Autowired
    private ModMailThreadManagementService modMailThreadManagementService;

    @Autowired
    private ModMailSubscriptionService modMailSubscriptionService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        ModMailThread modMailThread = modMailThreadManagementService.getByChannelId(commandContext.getChannel().getIdLong());
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(commandContext.getAuthor());
        modMailSubscriptionService.unsubscribeFromThread(aUserInAServer, modMailThread);
        return CommandResult.fromSuccess();
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        ModMailThread modMailThread = modMailThreadManagementService.getByChannelId(event.getChannel().getIdLong());
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(event.getMember());
        modMailSubscriptionService.unsubscribeFromThread(aUserInAServer, modMailThread);
        return interactionService.replyEmbed(UN_SUBSCRIBE_RESPONSE, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(ModMailSlashCommandNames.MODMAIL)
                .commandName(UN_SUBSCRIBE_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(UN_SUBSCRIBE_COMMAND)
                .slashCommandConfig(slashCommandConfig)
                .module(ModMailModuleDefinition.MODMAIL)
                .help(helpInfo)
                .supportsEmbedException(true)
                .templated(true)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModMailFeatureDefinition.MOD_MAIL;
    }

    @Override
    public List<CommandCondition> getConditions() {
        List<CommandCondition> conditions = super.getConditions();
        conditions.add(requiresModMailCondition);
        return conditions;
    }
}
