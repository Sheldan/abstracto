package dev.sheldan.abstracto.utility.commands.remind;

import dev.sheldan.abstracto.core.command.UtilityModuleInterface;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.*;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.utility.config.features.UtilityFeature;
import dev.sheldan.abstracto.utility.models.database.Reminder;
import dev.sheldan.abstracto.utility.models.template.commands.reminder.ReminderModel;
import dev.sheldan.abstracto.utility.service.ReminderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class Remind extends AbstractConditionableCommand {

    public static final String REMINDER_EMBED_KEY = "remind_response";

    @Autowired
    private ReminderService remindService;

    @Autowired
    private ChannelService channelService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        checkParameters(commandContext);
        List<Object> parameters = commandContext.getParameters().getParameters();
        Duration remindTime = (Duration) parameters.get(0);
        String text = (String) parameters.get(1);
        AUserInAServer aUserInAServer = commandContext.getUserInitiatedContext().getAUserInAServer();
        ReminderModel remindModel = (ReminderModel) ContextConverter.fromCommandContext(commandContext, ReminderModel.class);
        remindModel.setRemindText(text);
        Reminder createdReminder = remindService.createReminderInForUser(aUserInAServer, text, remindTime, commandContext.getMessage());
        remindModel.setReminder(createdReminder);

        log.info("Notifying user {} about reminder being scheduled.", commandContext.getAuthor().getId());

        return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInChannel(REMINDER_EMBED_KEY, remindModel, commandContext.getChannel()))
                .thenApply(aVoid -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("duration").type(Duration.class).templated(true).build());
        parameters.add(Parameter.builder().name("remindText").type(String.class).templated(true).remainder(true).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).hasExample(true).build();
        return CommandConfiguration.builder()
                .name("remind")
                .async(true)
                .module(UtilityModuleInterface.UTILITY)
                .templated(true)
                .supportsEmbedException(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return UtilityFeature.REMIND;
    }
}
