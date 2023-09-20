package dev.sheldan.abstracto.core.commands.help;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.condition.ConditionResult;
import dev.sheldan.abstracto.core.command.config.*;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.model.database.ACommand;
import dev.sheldan.abstracto.core.command.model.database.ACommandInAServer;
import dev.sheldan.abstracto.core.command.service.*;
import dev.sheldan.abstracto.core.command.service.management.CommandInServerManagementService;
import dev.sheldan.abstracto.core.command.service.management.CommandManagementService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.metric.service.MetricTag;
import dev.sheldan.abstracto.core.models.ServerIdChannelId;
import dev.sheldan.abstracto.core.models.template.commands.help.HelpCommandDetailsModel;
import dev.sheldan.abstracto.core.models.template.commands.help.HelpModuleDetailsModel;
import dev.sheldan.abstracto.core.models.template.commands.help.HelpModuleOverviewModel;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
public class Help extends AbstractConditionableCommand {

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

    @Autowired
    private CommandInServerAliasService commandInServerAliasService;

    @Autowired
    private CommandCoolDownService commandCoolDownService;

    public static final String HELP_COMMAND_EXECUTED_METRIC = "help.executions";
    public static final String CATEGORY = "category";
    private static final CounterMetric HELP_COMMAND_NO_PARAMETER_METRIC =
            CounterMetric
                    .builder()
                    .name(HELP_COMMAND_EXECUTED_METRIC)
                    .tagList(Arrays.asList(MetricTag.getTag(CATEGORY, "no.parameter")))
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
                ModuleDefinition moduleDefinition = moduleService.getModuleByName(parameter);
                log.debug("Displaying help for module {}.", moduleDefinition.getInfo().getName());
                SingleLevelPackedModule module = moduleService.getPackedModule(moduleDefinition);
                List<Command> filteredCommand = new ArrayList<>();
                List<CompletableFuture<ConditionResult>> conditionFutures = new ArrayList<>();
                Map<CompletableFuture<ConditionResult>, Command> futureCommandMap = new HashMap<>();
                module.getCommands().forEach(command -> {
                    // TODO dont provide the parameters, else the condition uses the wrong parameters, as we are not actually executing the command
                    CompletableFuture<ConditionResult> future = commandService.isCommandExecutable(command, commandContext);
                    conditionFutures.add(future);
                    futureCommandMap.put(future, command);
                });
                CompletableFutureList<ConditionResult> conditionFuturesList = new CompletableFutureList<>(conditionFutures);
                conditionFuturesList.getMainFuture().thenAccept(unused -> conditionFutures.forEach(conditionResultCompletableFuture -> {
                    if(!conditionResultCompletableFuture.isCompletedExceptionally()) {
                        ConditionResult result = conditionResultCompletableFuture.join();
                        if(result.isResult()) {
                            filteredCommand.add(futureCommandMap.get(conditionResultCompletableFuture));
                        }
                    }
                }));
                module.setCommands(filteredCommand);
                List<ModuleDefinition> subModules = moduleService.getSubModules(moduleDefinition);
                HelpModuleDetailsModel model = HelpModuleDetailsModel
                        .builder()
                        .subModules(subModules)
                        .module(module)
                        .build();
                MessageToSend messageToSend = templateService.renderEmbedTemplate("help_module_details_response", model, commandContext.getGuild().getIdLong());
                return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel()))
                        .thenApply(aVoid -> CommandResult.fromIgnored());
            } else {
                Optional<Command> commandOptional = commandRegistry.getCommandByNameOptional(parameter, true, commandContext.getGuild().getIdLong());
                if(commandOptional.isPresent()) {
                    metricService.incrementCounter(HELP_COMMAND_COMMAND_METRIC);
                    Command command = commandOptional.get();
                    ServerIdChannelId contextIds = ServerIdChannelId
                            .builder()
                            .channelId(commandContext.getChannel().getIdLong())
                            .serverId(commandContext.getGuild().getIdLong())
                            .build();

                    log.debug("Displaying help for command {}.", command.getConfiguration().getName());
                    ACommand aCommand = commandManagementService.findCommandByName(command.getConfiguration().getName());
                    List<String> aliases = commandInServerAliasService.getAliasesForCommand(commandContext.getGuild().getIdLong(), command.getConfiguration().getName());
                    ACommandInAServer aCommandInAServer = commandInServerManagementService.getCommandForServer(aCommand, commandContext.getGuild().getIdLong());
                    CommandCoolDownConfig coolDownConfig = getCoolDownConfig(command, contextIds);
                    HelpCommandDetailsModel model = HelpCommandDetailsModel
                            .builder()
                            .serverSpecificAliases(aliases)
                            .cooldowns(coolDownConfig)
                            .usage(commandService.generateUsage(command))
                            .command(command.getConfiguration())
                            .build();
                    if(Boolean.TRUE.equals(aCommandInAServer.getRestricted())) {
                        model.setAllowedRoles(roleService.getRolesFromGuild(aCommandInAServer.getAllowedRoles()));
                        model.setRestricted(true);
                    }
                    List<String> effects = command
                            .getConfiguration()
                            .getEffects()
                            .stream()
                            .map(EffectConfig::getEffectKey)
                            .toList();
                    if(!effects.isEmpty()) {
                        model.setEffects(effects);
                    }
                    MessageToSend messageToSend = templateService.renderEmbedTemplate("help_command_details_response", model, commandContext.getGuild().getIdLong());
                    return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel()))
                            .thenApply(aVoid -> CommandResult.fromIgnored());
                } else {
                    metricService.incrementCounter(HELP_COMMAND_WRONG_PARAM_METRIC);
                    return displayHelpOverview(commandContext);
                }
            }
        }
    }

    private CommandCoolDownConfig getCoolDownConfig(Command command, ServerIdChannelId contextIds) {
        Duration serverCooldown = commandCoolDownService.getServerCoolDownForCommand(command, contextIds.getServerId());
        Duration channelCooldown = commandCoolDownService.getChannelGroupCoolDownForCommand(command, contextIds);
        Duration memberCooldown = commandCoolDownService.getMemberCoolDownForCommand(command, contextIds);
        boolean hasMemberCooldown = !memberCooldown.equals(Duration.ZERO);
        boolean hasServerCoolDown = !serverCooldown.equals(Duration.ZERO);
        boolean hasChannelCoolDown = !channelCooldown.equals(Duration.ZERO);
        if(!hasMemberCooldown && !hasServerCoolDown && !hasChannelCoolDown) {
            return null;
        }
        return CommandCoolDownConfig
                .builder()
                .memberCoolDown(hasMemberCooldown ? memberCooldown : null)
                .serverCoolDown(hasServerCoolDown ? serverCooldown: null)
                .channelCoolDown(hasChannelCoolDown ? channelCooldown: null)
                .build();
    }

    private CompletableFuture<CommandResult> displayHelpOverview(CommandContext commandContext) {
        log.debug("Displaying help overview response.");
        ModuleDefinition moduleDefinition = moduleService.getDefaultModule();
        List<ModuleDefinition> subModules = moduleService.getSubModules(moduleDefinition);
        HelpModuleOverviewModel model = HelpModuleOverviewModel
                .builder()
                .modules(subModules)
                .build();
        MessageToSend messageToSend = templateService.renderEmbedTemplate("help_module_overview_response", model, commandContext.getGuild().getIdLong());
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
                .module(SupportModuleDefinition.SUPPORT)
                .parameters(Collections.singletonList(moduleOrCommandName))
                .help(helpInfo)
                .messageCommandOnly(true)
                .templated(true)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return CoreFeatureDefinition.CORE_FEATURE;
    }

    @PostConstruct
    public void postConstruct() {
        metricService.registerCounter(HELP_COMMAND_NO_PARAMETER_METRIC, "Help command executions without argument.");
        metricService.registerCounter(HELP_COMMAND_MODULE_METRIC, "Help command executions with a module as argument.");
        metricService.registerCounter(HELP_COMMAND_COMMAND_METRIC, "Help command executions with a command as argument.");
        metricService.registerCounter(HELP_COMMAND_WRONG_PARAM_METRIC, "Help command executions with a parameter not matching.");
    }
}
