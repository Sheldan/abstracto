package dev.sheldan.abstracto.core.commands.help;

import dev.sheldan.abstracto.command.*;
import dev.sheldan.abstracto.command.execution.*;
import dev.sheldan.abstracto.command.module.ModuleInfo;
import dev.sheldan.abstracto.templating.TemplateService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;


@Service
public class Help implements Command {


    @Autowired
    private ModuleRegistry registry;

    @Autowired
    private TemplateService templateService;

    @Override
    public Result execute(CommandContext commandContext) {
        CommandHierarchy commandStructure = registry.getDetailedModules();
        StringBuilder sb = new StringBuilder();
        if(commandContext.getParameters().getParameters().isEmpty()){
            sb.append("Help | Module overview \n");
            sb.append("```");
            commandStructure.getRootModules().forEach(packedModule -> {
                sb.append(getModule(packedModule, 0, true));
                sb.append("\n");
            });
            sb.append("```");
        } else {
            String parameterValue = commandContext.getParameters().getParameters().get(0).toString();
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

        commandContext.getChannel().sendMessage(sb.toString()).queue();
        return Result.fromSuccess();
    }

    private String getCommand(Command command){
        StringBuilder sb = new StringBuilder();
        CommandConfiguration commandConfiguration = command.getConfiguration();
        sb.append(String.format("Command: **%s**", commandConfiguration.getName()));
        sb.append("\n");
        sb.append(String.format("Description: %s", getTemplateOrDefault(commandConfiguration.getDescriptionTemplate(), commandConfiguration.getDescription())));
        sb.append("\n");
        HelpInfo helpObj = commandConfiguration.getHelp();
        if(helpObj != null){
            sb.append(String.format("Usage: %s", getTemplateOrDefault(helpObj.getUsageTemplate(), helpObj.getUsage())));
            sb.append("\n");
            sb.append(String.format("Detailed help: %s", getTemplateOrDefault(helpObj.getLongHelpTemplate(), helpObj.getLongHelp())));
            sb.append("\n");
        }
        return sb.toString();
    }

    private String getTemplateOrDefault(String templateKey, String defaultText) {
        if(templateKey == null) {
            return defaultText;
        } else {
            return templateService.renderTemplate(templateKey, null);
        }
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
    public CommandConfiguration getConfiguration() {
        Parameter moduleOrCommandName = Parameter.builder()
                .name("name")
                .optional(true)
                .description("Name of module or command")
                .type(String.class)
                .build();

        return CommandConfiguration.builder()
                .name("help")
                .module("support")
                .parameters(Collections.singletonList(moduleOrCommandName))
                .description("Prints the help")
                .causesReaction(false)
                .build();
    }

}
