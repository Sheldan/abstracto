package dev.sheldan.abstracto.utility.command;

import dev.sheldan.abstracto.command.Command;
import dev.sheldan.abstracto.command.HelpInfo;
import dev.sheldan.abstracto.command.execution.CommandConfiguration;
import dev.sheldan.abstracto.command.execution.CommandContext;
import dev.sheldan.abstracto.command.execution.Parameter;
import dev.sheldan.abstracto.command.execution.Result;
import dev.sheldan.abstracto.core.models.embed.MessageToSend;
import dev.sheldan.abstracto.templating.TemplateService;
import dev.sheldan.abstracto.utility.Utility;
import dev.sheldan.abstracto.utility.models.template.starboard.StarStatsModel;
import dev.sheldan.abstracto.utility.service.StarboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class StarStats implements Command {

    public static final String STARSTATS_RESPONSE_TEMPLATE = "starStats_response";
    @Autowired
    private StarboardService starboardService;

    @Autowired
    private TemplateService templateService;

    @Override
    public Result execute(CommandContext commandContext) {
        StarStatsModel result = starboardService.retrieveStarStats(commandContext.getGuild().getIdLong());
        MessageToSend messageToSend = templateService.renderEmbedTemplate(STARSTATS_RESPONSE_TEMPLATE, result);
        commandContext.getChannel().sendMessage(messageToSend.getEmbed()).queue();
        return Result.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("starStats")
                .module(Utility.UTILITY)
                .templated(true)
                .causesReaction(false)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }
}
