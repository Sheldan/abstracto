package dev.sheldan.abstracto.command.support;

import dev.sheldan.abstracto.command.Command;
import dev.sheldan.abstracto.command.execution.Configuration;
import dev.sheldan.abstracto.command.execution.Context;
import dev.sheldan.abstracto.command.execution.Result;
import dev.sheldan.abstracto.command.meta.CommandRegistry;
import net.dv8tion.jda.api.EmbedBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Help implements Command {


    @Autowired
    private CommandRegistry registry;

    @Override
    public Result execute(Context context) {
        EmbedBuilder builder = new EmbedBuilder();
        registry.getAllCommands().forEach(command -> {
            builder.addField(command.getConfiguration().getName(), command.getConfiguration().getDescription(), false);
        });
        context.getChannel().sendMessage(builder.build()).queue();
        return Result.fromSuccess();
    }

    @Override
    public Configuration getConfiguration() {
        return Configuration.builder()
                .name("help")
                .module("utility")
                .description("Prints the help")
                .causesReaction(false)
                .build();
    }
}
