package dev.sheldan.abstracto.commands.management;

import dev.sheldan.abstracto.command.Command;
import dev.sheldan.abstracto.command.execution.Context;
import dev.sheldan.abstracto.command.execution.Parameter;
import dev.sheldan.abstracto.command.execution.Parameters;
import dev.sheldan.abstracto.command.meta.UnParsedCommandParameter;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class CommandReceivedHandler extends ListenerAdapter {

    @Autowired
    private CommandManager manager;

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if(!manager.isCommand(event.getMessage())) {
            return;
        }
        List<String> parameters = Arrays.asList(event.getMessage().getContentStripped().split(" "));
        UnParsedCommandParameter unparsedParameter = new UnParsedCommandParameter();
        unparsedParameter.setParameters(parameters.subList(1, parameters.size()));
        String withoutPrefix = parameters.get(0).substring(1);
        Command foundCommand = manager.findCommandByParameters(withoutPrefix, unparsedParameter);
        Parameters parsedParameters = getParsedParameters(unparsedParameter, foundCommand);
        Context context = Context.builder()
                .author(event.getAuthor())
                .channel(event.getTextChannel())
                .message(event.getMessage())
                .parameters(parsedParameters)
                .jda(event.getJDA())
                .build();
        foundCommand.execute(context);
    }

    public Parameters getParsedParameters(UnParsedCommandParameter unParsedCommandParameter, Command command){
        List<Object> parsedParameters = new ArrayList<>();
        for (int i = 0; i < unParsedCommandParameter.getParameters().size(); i++) {
            Parameter param = command.getConfiguration().getParameters().get(i);
            String value = unParsedCommandParameter.getParameters().get(i);
            if(param.getType().equals(Integer.class)){
                parsedParameters.add(Integer.parseInt(value));
            } else if(param.getType().equals(Double.class)){
                parsedParameters.add(Double.parseDouble(value));
            } else {
                parsedParameters.add(value);
            }
        }
        return Parameters.builder().parameters(parsedParameters).build();
    }
}
