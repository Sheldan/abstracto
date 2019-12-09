package dev.sheldan.abstracto.command.utility;

import dev.sheldan.abstracto.command.Command;
import dev.sheldan.abstracto.command.execution.Configuration;
import dev.sheldan.abstracto.command.execution.Context;
import dev.sheldan.abstracto.command.execution.Result;
import org.springframework.stereotype.Service;

@Service
public class Ping implements Command {

    @Override
    public Result execute(Context context) {
        long ping = context.getJda().getGatewayPing();
        context.getChannel().sendMessage("Latency: " + ping + " ms.").queue();
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
