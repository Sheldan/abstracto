package dev.sheldan.abstracto.command.support;

import dev.sheldan.abstracto.command.Command;
import dev.sheldan.abstracto.command.CommandHierarchy;
import dev.sheldan.abstracto.command.ModuleRegistry;
import dev.sheldan.abstracto.command.PackedModule;
import dev.sheldan.abstracto.command.execution.*;
import dev.sheldan.abstracto.command.module.ModuleInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;


@Service
public class Help implements Command {


    @Autowired
    private ModuleRegistry registry;

    @Override
    public Result execute(Context context) {
        CommandHierarchy commandStructure = registry.getDetailedModules();
        StringBuilder sb = new StringBuilder();
        if(context.getParameters().getParameters().isEmpty()){
            sb.append("Help | Module overview \n");
            sb.append("```");
            commandStructure.getRootModules().forEach(packedModule -> {
                sb.append(getModule(packedModule, 0, true));
                sb.append("\n");
            });
            sb.append("```");
        } else {
            String parameterValue = context.getParameters().getParameters().get(0).toString();
            PackedModule module = commandStructure.getModuleWithName(parameterValue);
            if(module != null){
                sb.append("Help | Module overview \n");
                sb.append(getModule(module, 0, false));
                module.getCommands().forEach(command -> {
                    sb.append(getCommand(command));
                });
            } else {
                Command command = commandStructure.getCommandWithName(parameterValue);
                if(command != null) {
                    sb.append("Help | Command overview");
                    sb.append("\n");
                    sb.append(getCommand(command));
                }
            }
        }

        context.getChannel().sendMessage(sb.toString()).queue();
        return Result.fromSuccess();
    }

    private String getCommand(Command command){
        StringBuilder sb = new StringBuilder();
        Configuration configuration = command.getConfiguration();
        sb.append(String.format("Command: **%s**", configuration.getName()));
        sb.append("\n");
        sb.append(String.format("Description: %s", configuration.getDescription()));
        sb.append("\n");
        if(configuration.getHelp() != null){
            sb.append(String.format("Usage: %s", configuration.getHelp().getUsage()));
            sb.append("\n");
            sb.append(String.format("Detailed help: %s", configuration.getHelp().getLongHelp()));
        }
        return sb.toString();
    }

    private String getModule(PackedModule module, int depth, boolean recursive){
        StringBuilder sb = new StringBuilder();
        String intentation = "";
        if(depth > 0){
          intentation = StringUtils.repeat("-", depth) + ">";
        }
        ModuleInfo info = module.getModule().getInfo();
        sb.append(String.format(intentation +"**%s** \n", info.getName()));
        sb.append(String.format(intentation + "%s \n", info.getDescription()));
        if(recursive) {
            module.getSubModules().forEach(subModule -> {
                sb.append(getModule(subModule, depth + 1, true));
            });
        }
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public Configuration getConfiguration() {
        Parameter moduleOrCommandName = Parameter.builder()
                .name("name")
                .optional(true)
                .description("Name of module or command")
                .type(String.class)
                .build();

        return Configuration.builder()
                .name("help")
                .module("support")
                .parameters(Collections.singletonList(moduleOrCommandName))
                .description("Prints the help")
                .causesReaction(false)
                .build();
    }

}
