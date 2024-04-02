package dev.sheldan.abstracto.remind.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.UserCommandConfig;
import dev.sheldan.abstracto.core.interaction.ComponentPayloadService;
import dev.sheldan.abstracto.core.interaction.ComponentService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.template.display.UserDisplay;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.ContextUtils;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.core.utils.ParseUtils;
import dev.sheldan.abstracto.core.utils.SnowflakeUtils;
import dev.sheldan.abstracto.remind.config.RemindFeatureDefinition;
import dev.sheldan.abstracto.remind.config.RemindSlashCommandNames;
import dev.sheldan.abstracto.remind.model.database.Reminder;
import dev.sheldan.abstracto.remind.model.template.commands.ReminderDisplay;
import dev.sheldan.abstracto.remind.model.template.commands.ReminderModel;
import dev.sheldan.abstracto.remind.payload.JoinReminderPayload;
import dev.sheldan.abstracto.remind.service.ReminderService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class Remind extends AbstractConditionableCommand {

    public static final String REMINDER_EMBED_KEY = "remind_response";
    public static final String DURATION_PARAMETER = "duration";
    public static final String REMIND_TEXT_PARAMETER = "remindText";
    public static final String REMINDER_JOIN_BUTTON_ORIGIN = "REMINDER_JOIN_BUTTON";

    @Autowired
    private ReminderService remindService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private ComponentService componentService;

    @Autowired
    private ComponentPayloadService componentPayloadService;

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Duration remindTime = (Duration) parameters.get(0);
        String text = null;
        if(parameters.size() > 1) {
            text = (String) parameters.get(1);
        }
        Long serverId = commandContext.getGuild().getIdLong();
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(commandContext.getAuthor());
        Reminder createdReminder = remindService.createReminderInForUser(aUserInAServer, text, remindTime, commandContext.getMessage());
        String joinButtonId = componentService.generateComponentId();
        ReminderModel remindModel = ReminderModel
                .builder()
                .remindText(text)
                .userDisplay(UserDisplay.fromUser(commandContext.getAuthor().getUser()))
                .joinButtonId(joinButtonId)
                .reminder(ReminderDisplay.fromReminder(createdReminder))
                .message(ServerChannelMessage.fromMessage(commandContext.getMessage()))
                .build();

        JoinReminderPayload payload = JoinReminderPayload
                .builder()
                .remindedUserId(commandContext.getAuthor().getIdLong())
                .reminderId(createdReminder.getId())
                .serverId(serverId)
                .build();

        componentPayloadService.createButtonPayload(joinButtonId, payload, REMINDER_JOIN_BUTTON_ORIGIN, aUserInAServer.getServerReference());

        log.info("Notifying user {} about reminder being scheduled.", commandContext.getAuthor().getId());
        MessageToSend messageToSend = templateService.renderEmbedTemplate(REMINDER_EMBED_KEY, remindModel, serverId);
        return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel()))
                .thenApply(aVoid -> CommandResult.fromSuccess());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String durationString = slashCommandParameterService.getCommandOption(DURATION_PARAMETER, event, Duration.class, String.class);
        Duration duration = ParseUtils.parseDuration(durationString);
        String reminderText = null;
        Long serverId = event.getGuild().getIdLong();
        if(slashCommandParameterService.hasCommandOption(REMIND_TEXT_PARAMETER, event)) {
            reminderText = slashCommandParameterService.getCommandOption(REMIND_TEXT_PARAMETER, event, String.class, String.class);
        }
        String joinButtonId;
        Reminder createdReminder;
        if(ContextUtils.isUserCommand(event)) {
            joinButtonId = null;
            AUser aUser = userManagementService.loadOrCreateUser(event.getUser().getIdLong());
            createdReminder = remindService.createReminderInForUser(aUser, reminderText, duration);
        } else {
            joinButtonId = componentService.generateComponentId();
            Long snowFlake = SnowflakeUtils.createSnowFlake();
            AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(event.getMember());
            createdReminder = remindService.createReminderInForUser(aUserInAServer, reminderText, duration, event.getChannel().getIdLong(), snowFlake);
        }
        ReminderModel remindModel = ReminderModel
                .builder()
                .remindText(reminderText)
                .joinButtonId(joinButtonId)
                .userDisplay(UserDisplay.fromUser(event.getUser()))
                .reminder(ReminderDisplay.fromReminder(createdReminder))
                .build();

        if(ContextUtils.isNotUserCommand(event)) {
            AServer server = serverManagementService.loadServer(serverId);
            JoinReminderPayload payload = JoinReminderPayload
                    .builder()
                    .remindedUserId(event.getUser().getIdLong())
                    .reminderId(createdReminder.getId())
                    .serverId(serverId)
                    .build();
            componentPayloadService.createButtonPayload(joinButtonId, payload, REMINDER_JOIN_BUTTON_ORIGIN, server);
        }



        log.info("Notifying user {} about reminder being scheduled.", event.getUser().getId());
        MessageToSend messageToSend = templateService.renderEmbedTemplate(REMINDER_EMBED_KEY, remindModel, ContextUtils.serverIdOrNull(event));
        return interactionService.replyMessageToSend(messageToSend, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter durationParameter = Parameter
                .builder()
                .name(DURATION_PARAMETER)
                .type(Duration.class)
                .templated(true)
                .build();
        Parameter remindTextParameter = Parameter
                .builder()
                .name(REMIND_TEXT_PARAMETER)
                .type(String.class)
                .templated(true)
                .optional(true)
                .remainder(true)
                .build();
        List<Parameter> parameters = Arrays.asList(durationParameter, remindTextParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .hasExample(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .userInstallable(true)
                .userCommandConfig(UserCommandConfig.all())
                .rootCommandName(RemindSlashCommandNames.REMIND)
                .commandName("create")
                .build();

        return CommandConfiguration.builder()
                .name("remind")
                .async(true)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .slashCommandConfig(slashCommandConfig)
                .supportsEmbedException(true)
                .causesReaction(false)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return RemindFeatureDefinition.REMIND;
    }
}
