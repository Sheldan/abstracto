package dev.sheldan.abstracto.core.commands.help;

import dev.sheldan.abstracto.core.command.*;
import dev.sheldan.abstracto.core.command.config.*;
import dev.sheldan.abstracto.core.command.execution.*;
import dev.sheldan.abstracto.core.command.service.CommandRegistry;
import dev.sheldan.abstracto.core.command.service.ModuleRegistry;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.features.CoreFeatures;
import dev.sheldan.abstracto.core.models.template.commands.help.HelpCommandDetailsModel;
import dev.sheldan.abstracto.core.models.template.commands.help.HelpModuleDetailsModel;
import dev.sheldan.abstracto.core.models.template.commands.help.HelpModuleOverviewModel;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;


@Service
public class Help implements Command {


    @Autowired
    private ModuleRegistry moduleService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private CommandRegistry commandRegistry;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        if(parameters.isEmpty()) {
            ModuleInterface moduleInterface = moduleService.getDefaultModule();
            List<ModuleInterface> subModules = moduleService.getSubModules(moduleInterface);
            HelpModuleOverviewModel model = (HelpModuleOverviewModel) ContextConverter.fromCommandContext(commandContext, HelpModuleOverviewModel.class);
            model.setModules(subModules);
            MessageToSend messageToSend = templateService.renderEmbedTemplate("help_module_overview_response", model);
            channelService.sendMessageToEndInTextChannel(messageToSend, commandContext.getChannel());
        } else {
            String parameter = (String) parameters.get(0);
            if(moduleService.moduleExists(parameter)){
                ModuleInterface moduleInterface = moduleService.getModuleByName(parameter);
                SingleLevelPackedModule module = moduleService.getPackedModule(moduleInterface);
                List<ModuleInterface> subModules = moduleService.getSubModules(moduleInterface);
                HelpModuleDetailsModel model = (HelpModuleDetailsModel) ContextConverter.fromCommandContext(commandContext, HelpModuleDetailsModel.class);
                model.setModule(module);
                model.setSubModules(subModules);
                MessageToSend messageToSend = templateService.renderEmbedTemplate("help_module_details_response", model);
                channelService.sendMessageToEndInTextChannel(messageToSend, commandContext.getChannel());
            } else if(commandRegistry.commandExists(parameter)) {
                Command command = commandRegistry.getCommandByName(parameter);
                HelpCommandDetailsModel model = (HelpCommandDetailsModel) ContextConverter.fromCommandContext(commandContext, HelpCommandDetailsModel.class);
                model.setCommand(command.getConfiguration());
                MessageToSend messageToSend = templateService.renderEmbedTemplate("help_command_details_response", model);
                channelService.sendMessageToEndInTextChannel(messageToSend, commandContext.getChannel());
            }
        }
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter moduleOrCommandName = Parameter.builder()
                .name("name")
                .optional(true)
                .description("Name of module or command")
                .type(String.class)
                .build();
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("help")
                .module("support")
                .parameters(Collections.singletonList(moduleOrCommandName))
                .help(helpInfo)
                .templated(true)
                .causesReaction(false)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return CoreFeatures.CORE_FEATURE;
    }
}
