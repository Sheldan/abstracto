package dev.sheldan.abstracto.core.commands.utility;

import dev.sheldan.abstracto.command.Command;
import dev.sheldan.abstracto.command.execution.CommandConfiguration;
import dev.sheldan.abstracto.command.execution.CommandContext;
import dev.sheldan.abstracto.command.execution.Result;
import dev.sheldan.abstracto.core.commands.utility.model.PingModel;
import dev.sheldan.abstracto.templating.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Ping implements Command {

    public static final String PING_TEMPLATE = "ping";

    @Autowired
    private TemplateService templateService;

    @Override
    public Result execute(CommandContext commandContext) {
        long ping = commandContext.getJda().getGatewayPing();
        PingModel model = PingModel.parentBuilder().parent(commandContext.getCommandTemplateContext()).latency(ping).build();
        String text = templateService.renderTemplate(PING_TEMPLATE, model);
        commandContext.getChannel().sendMessage(text).queue();
        return Result.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        return CommandConfiguration.builder()
                .name("ping")
                .module("utility")
                .templated(true)
                .causesReaction(false)
                .build();
    }

}
