package dev.sheldan.abstracto.moderation.commands;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ContextConverter;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.moderation.config.ModerationModule;
import dev.sheldan.abstracto.moderation.config.features.ModerationFeatures;
import dev.sheldan.abstracto.moderation.models.template.commands.MyWarningsModel;
import dev.sheldan.abstracto.moderation.service.management.WarnManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class MyWarnings extends AbstractConditionableCommand {

    public static final String MY_WARNINGS_RESPONSE_EMBED_TEMPLATE = "myWarnings_response";
    @Autowired
    private ChannelService channelService;

    @Autowired
    private WarnManagementService warnManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        MyWarningsModel model = (MyWarningsModel) ContextConverter.fromCommandContext(commandContext, MyWarningsModel.class);
        AUserInAServer userInAServer = userInServerManagementService.loadUser(commandContext.getAuthor());
        Long currentWarnCount = warnManagementService.getActiveWarnsForUser(userInAServer);
        model.setCurrentWarnCount(currentWarnCount);
        Long totalWarnCount = warnManagementService.getTotalWarnsForUser(userInAServer);
        model.setTotalWarnCount(totalWarnCount);
        channelService.sendEmbedTemplateInChannel(MY_WARNINGS_RESPONSE_EMBED_TEMPLATE, model, commandContext.getChannel());
        return CommandResult.fromIgnored();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        List<String> aliases = Arrays.asList("myWarns");
        return CommandConfiguration.builder()
                .name("myWarnings")
                .module(ModerationModule.MODERATION)
                .templated(true)
                .supportsEmbedException(true)
                .causesReaction(true)
                .aliases(aliases)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return ModerationFeatures.WARNING;
    }
}
