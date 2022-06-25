package dev.sheldan.abstracto.remind.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.remind.config.RemindFeatureDefinition;
import dev.sheldan.abstracto.remind.config.RemindSlashCommandNames;
import dev.sheldan.abstracto.remind.model.database.Reminder;
import dev.sheldan.abstracto.remind.model.template.commands.ReminderDisplay;
import dev.sheldan.abstracto.remind.model.template.commands.RemindersModel;
import dev.sheldan.abstracto.remind.service.management.ReminderManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
public class Reminders extends AbstractConditionableCommand {

    public static final String REMINDERS_RESPONSE_TEMPLATE = "reminders_response";
    @Autowired
    private ReminderManagementService reminderManagementService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        Long serverId = commandContext.getGuild().getIdLong();
        Member member = commandContext.getAuthor();
        MessageToSend messageToSend = getMessageToSend(serverId, member);
        return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel()))
                .thenApply(aVoid -> CommandResult.fromIgnored());
    }

    private MessageToSend getMessageToSend(Long serverId, Member member) {
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(member);
        List<Reminder> activeReminders = reminderManagementService.getActiveRemindersForUser(aUserInAServer);
        List<ReminderDisplay> reminders = activeReminders.stream().map(reminder -> {
            ServerChannelMessage originMessage = ServerChannelMessage
                    .builder()
                    .messageId(reminder.getMessageId())
                    .channelId(reminder.getChannel().getId())
                    .serverId(serverId)
                    .build();
            return ReminderDisplay
                    .builder()
                    .reminder(reminder)
                    .message(originMessage)
                    .build();
        }).collect(Collectors.toList());
        RemindersModel model = RemindersModel
                .builder()
                .reminders(reminders)
                .member(member)
                .build();
        log.info("Showing {} reminders for user {} in server {}.", activeReminders.size(), aUserInAServer.getUserReference().getId(), serverId);
        return templateService.renderEmbedTemplate(REMINDERS_RESPONSE_TEMPLATE, model, serverId);
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Long serverId = event.getGuild().getIdLong();
        Member member = event.getMember();
        MessageToSend messageToSend = getMessageToSend(serverId, member);
        return interactionService.replyMessageToSend(messageToSend, event)
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
                .rootCommandName(RemindSlashCommandNames.REMIND)
                .commandName("list")
                .build();

        return CommandConfiguration.builder()
                .name("reminders")
                .async(true)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .slashCommandConfig(slashCommandConfig)
                .supportsEmbedException(true)
                .causesReaction(true)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return RemindFeatureDefinition.REMIND;
    }
}
