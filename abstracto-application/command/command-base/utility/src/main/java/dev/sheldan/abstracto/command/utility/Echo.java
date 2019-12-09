package dev.sheldan.abstracto.command.utility;

import dev.sheldan.abstracto.command.Command;
import dev.sheldan.abstracto.command.execution.Configuration;
import dev.sheldan.abstracto.command.execution.Context;
import dev.sheldan.abstracto.command.execution.Parameter;
import dev.sheldan.abstracto.command.execution.Result;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class Echo implements Command {

    @Override
    public Result execute(Context context) {
        StringBuilder sb = new StringBuilder();
        context.getParameters().getParameters().forEach(o -> {
            sb.append(o.toString());
        });
        context.getChannel().sendMessage(sb.toString()).queue();
        return Result.fromSuccess();
    }

    @Override
    public Configuration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("input").type(String.class).remainder(true).build());
        return Configuration.builder()
                .name("echo")
                .module("utility")
                .description("Echos the input back to the same channel")
                .causesReaction(false)
                .parameters(parameters)
                .build();
    }
}
