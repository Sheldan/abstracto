package dev.sheldan.abstracto.core.commands.utility;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.UtilityModuleInterface;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatures;
import dev.sheldan.abstracto.core.models.template.commands.PingModel;
import dev.sheldan.abstracto.templating.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Ping implements Command {

    public static final String PING_TEMPLATE = "ping_response";

    @Autowired
    private TemplateService templateService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        long ping = commandContext.getJda().getGatewayPing();
        PingModel model = PingModel.builder().latency(ping).build();
        String text = templateService.renderTemplate(PING_TEMPLATE, model);
        commandContext.getChannel().sendMessage(text).queue();
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("ping")
                .module(UtilityModuleInterface.UTILITY)
                .templated(true)
                .help(helpInfo)
                .causesReaction(false)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return CoreFeatures.CORE_FEATURE;
    }

}
