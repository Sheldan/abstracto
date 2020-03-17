package dev.sheldan.abstracto.command.utility;

import dev.sheldan.abstracto.command.Command;
import dev.sheldan.abstracto.command.execution.Configuration;
import dev.sheldan.abstracto.command.execution.CommandContext;
import dev.sheldan.abstracto.command.execution.Result;
import dev.sheldan.abstracto.command.utility.model.PingModel;
import dev.sheldan.abstracto.templating.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class Ping implements Command {

    public static final String PING_TEMPLATE = "ping";

    @Autowired
    private TemplateService templateService;

    @Override
    @Transactional
    public Result execute(CommandContext commandContext) {
        long ping = commandContext.getJda().getGatewayPing();
        PingModel model = PingModel.parentBuilder().parent(commandContext.getCommandTemplateContext()).latency(ping).build();
        String text = templateService.renderTemplate(PING_TEMPLATE, model);
        commandContext.getChannel().sendMessage(text).queue();
        return Result.fromSuccess();
    }

    @Override
    public Configuration getConfiguration() {
        return Configuration.builder()
                .name("ping")
                .module("utility")
                .description("Prints the current bot latency to the discord api")
                .causesReaction(false)
                .build();
    }

}
