package dev.sheldan.abstracto.command.utility;

import dev.sheldan.abstracto.command.Command;
import dev.sheldan.abstracto.command.execution.Configuration;
import dev.sheldan.abstracto.command.execution.Context;
import dev.sheldan.abstracto.command.execution.Result;
import dev.sheldan.abstracto.templating.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;

@Service
public class Ping implements Command {

    public static final String PING_TEMPLATE = "ping";

    @Autowired
    private TemplateService templateService;

    @Override
    @Transactional
    public Result execute(Context context) {
        long ping = context.getJda().getGatewayPing();
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("latency", ping);
        String text = templateService.renderTemplate(PING_TEMPLATE, parameters);
        context.getChannel().sendMessage(text).queue();
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
