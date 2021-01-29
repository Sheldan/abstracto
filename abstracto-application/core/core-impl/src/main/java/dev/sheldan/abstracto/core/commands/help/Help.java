package dev.sheldan.abstracto.core.commands.help;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.*;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatures;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ContextConverter;
import dev.sheldan.abstracto.core.command.models.database.ACommand;
import dev.sheldan.abstracto.core.command.models.database.ACommandInAServer;
import dev.sheldan.abstracto.core.command.service.CommandRegistry;
import dev.sheldan.abstracto.core.command.service.CommandService;
import dev.sheldan.abstracto.core.command.service.ModuleRegistry;
import dev.sheldan.abstracto.core.command.service.management.CommandInServerManagementService;
import dev.sheldan.abstracto.core.command.service.management.CommandManagementService;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.metrics.service.CounterMetric;
import dev.sheldan.abstracto.core.metrics.service.MetricService;
import dev.sheldan.abstracto.core.metrics.service.MetricTag;
import dev.sheldan.abstracto.core.models.template.commands.help.HelpCommandDetailsModel;
import dev.sheldan.abstracto.core.models.template.commands.help.HelpModuleDetailsModel;
import dev.sheldan.abstracto.core.models.template.commands.help.HelpModuleOverviewModel;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@Service
@Slf4j
public class Help implements Command {


    @Autowired
    private ModuleRegistry moduleService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private CommandRegistry commandRegistry;

    @Autowired
    private CommandInServerManagementService commandInServerManagementService;

    @Autowired
    private CommandManagementService commandManagementService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private CommandService commandService;

    @Autowired
    private MetricService metricService;

    public static final String HELP_COMMAND_EXECUTED_METRIC = "help.executions";
    public static final String CATEGORY = "category";
    private static final CounterMetric HELP_COMMAND_NO_PARAMETER_METRIC =
            CounterMetric
                    .builder()
                    .name(HELP_COMMAND_EXECUTED_METRIC)
                    .tagList(Arrays.asList(MetricTag.getTag(CATEGORY, "no.parmeter")))
                    .build();

    private static final CounterMetric HELP_COMMAND_MODULE_METRIC =
            CounterMetric
                    .builder()
                    .name(HELP_COMMAND_EXECUTED_METRIC)
                    .tagList(Arrays.asList(MetricTag.getTag(CATEGORY, "module")))
                    .build();

    private static final CounterMetric HELP_COMMAND_COMMAND_METRIC =
            CounterMetric
                    .builder()
                    .name(HELP_COMMAND_EXECUTED_METRIC)
                    .tagList(Arrays.asList(MetricTag.getTag(CATEGORY, "command")))
                    .build();

    private static final CounterMetric HELP_COMMAND_WRONG_PARAM_METRIC =
            CounterMetric
                    .builder()
                    .name(HELP_COMMAND_EXECUTED_METRIC)
                    .tagList(Arrays.asList(MetricTag.getTag(CATEGORY, "wrong.argument")))
                    .build();

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        if(parameters.isEmpty()) {
            metricService.incrementCounter(HELP_COMMAND_NO_PARAMETER_METRIC);
            return displayHelpOverview(commandContext);
        } else {
            String parameter = (String) parameters.get(0);
            if(moduleService.moduleExists(parameter)){
                metricService.incrementCounter(HELP_COMMAND_MODULE_METRIC);
                ModuleInterface moduleInterface = moduleService.getModuleByName(parameter);
                log.trace("Displaying help for module {}.", moduleInterface.getInfo().getName());
                SingleLevelPackedModule module = moduleService.getPackedModule(moduleInterface);
                List<Command> commands = module.getCommands();
                List<Command> filteredCommands = new ArrayList<>();
                commands.forEach(command -> {
                    if(commandService.isCommandExecutable(command, commandContext).isResult()) {
                        filteredCommands.add(command);
                    }
                });
                module.setCommands(filteredCommands);
                List<ModuleInterface> subModules = moduleService.getSubModules(moduleInterface);
                HelpModuleDetailsModel model = (HelpModuleDetailsModel) ContextConverter.fromCommandContext(commandContext, HelpModuleDetailsModel.class);
                model.setModule(module);
                model.setSubModules(subModules);
                MessageToSend messageToSend = templateService.renderEmbedTemplate("help_module_details_response", model);
                return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel()))
                        .thenApply(aVoid -> CommandResult.fromIgnored());
            } else if(commandRegistry.commandExists(parameter)) {
                metricService.incrementCounter(HELP_COMMAND_COMMAND_METRIC);
                Command command = commandRegistry.getCommandByName(parameter);
                log.trace("Displaying help for command {}.", command.getConfiguration().getName());
                ACommand aCommand = commandManagementService.findCommandByName(parameter);
                ACommandInAServer aCommandInAServer = commandInServerManagementService.getCommandForServer(aCommand, commandContext.getGuild().getIdLong());
                HelpCommandDetailsModel model = (HelpCommandDetailsModel) ContextConverter.fromCommandContext(commandContext, HelpCommandDetailsModel.class);
                if(Boolean.TRUE.equals(aCommandInAServer.getRestricted())) {
                    model.setImmuneRoles(roleService.getRolesFromGuild(aCommandInAServer.getImmuneRoles()));
                    model.setAllowedRoles(roleService.getRolesFromGuild(aCommandInAServer.getAllowedRoles()));
                    model.setRestricted(true);
                }
                model.setUsage(commandService.generateUsage(command));
                model.setCommand(command.getConfiguration());
                MessageToSend messageToSend = templateService.renderEmbedTemplate("help_command_details_response", model);
                return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel()))
                        .thenApply(aVoid -> CommandResult.fromIgnored());
            } else {
                metricService.incrementCounter(HELP_COMMAND_WRONG_PARAM_METRIC);
                return displayHelpOverview(commandContext);
            }
        }
    }

    private CompletableFuture<CommandResult> displayHelpOverview(CommandContext commandContext) {
        log.trace("Displaying help overview response.");
        ModuleInterface moduleInterface = moduleService.getDefaultModule();
        List<ModuleInterface> subModules = moduleService.getSubModules(moduleInterface);
        HelpModuleOverviewModel model = (HelpModuleOverviewModel) ContextConverter.fromCommandContext(commandContext, HelpModuleOverviewModel.class);
        model.setModules(subModules);
        MessageToSend messageToSend = templateService.renderEmbedTemplate("help_module_overview_response", model);
        return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel()))
                .thenApply(aVoid -> CommandResult.fromIgnored());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter moduleOrCommandName = Parameter.builder()
                .name("name")
                .optional(true)
                .templated(true)
                .type(String.class)
                .build();
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("help")
                .async(true)
                .module(SupportModuleInterface.SUPPORT)
                .parameters(Collections.singletonList(moduleOrCommandName))
                .help(helpInfo)
                .templated(true)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return CoreFeatures.CORE_FEATURE;
    }

    @PostConstruct
    public void postConstruct() {
        metricService.registerCounter(HELP_COMMAND_NO_PARAMETER_METRIC, "Help command executions without argument.");
        metricService.registerCounter(HELP_COMMAND_MODULE_METRIC, "Help command executions with a module as argument.");
        metricService.registerCounter(HELP_COMMAND_COMMAND_METRIC, "Help command executions with a command as argument.");
        metricService.registerCounter(HELP_COMMAND_WRONG_PARAM_METRIC, "Help command executions with a parameter not matching.");
    }
}
