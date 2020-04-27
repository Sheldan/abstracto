package dev.sheldan.abstracto.utility.commands.remind;

import dev.sheldan.abstracto.core.command.UtilityModuleInterface;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.*;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.utility.config.features.UtilityFeature;
import dev.sheldan.abstracto.utility.models.template.commands.reminder.ReminderModel;
import dev.sheldan.abstracto.utility.service.ReminderService;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class Remind extends AbstractConditionableCommand {

    @Autowired
    private ReminderService remindService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Duration remindTime = (Duration) parameters.get(0);
        String text = (String) parameters.get(1);
        AUserInAServer aUserInAServer = commandContext.getUserInitiatedContext().getAUserInAServer();
        ReminderModel remindModel = (ReminderModel) ContextConverter.fromCommandContext(commandContext, ReminderModel.class);
        remindModel.setMessage(commandContext.getMessage());
        remindModel.setRemindText(text);
        remindService.createReminderInForUser(aUserInAServer, text, remindTime, remindModel);
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("remindTime").type(Duration.class).build());
        parameters.add(Parameter.builder().name("remindText").type(String.class).maxLength(MessageEmbed.TEXT_MAX_LENGTH).remainder(true).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("remind")
                .module(UtilityModuleInterface.UTILITY)
                .templated(true)
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
