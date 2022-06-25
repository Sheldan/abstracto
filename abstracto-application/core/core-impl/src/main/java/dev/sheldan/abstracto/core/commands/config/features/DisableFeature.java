package dev.sheldan.abstracto.core.commands.config.features;

import dev.sheldan.abstracto.core.interaction.slash.CoreSlashCommandNames;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.condition.CommandCondition;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.commands.config.ConfigModuleDefinition;
import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.template.commands.FeatureSwitchModel;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class DisableFeature extends AbstractConditionableCommand {

    public static final String DISABLE_FEATURE_COMMAND = "disableFeature";
    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    private static final String DISABLE_FEATURE_DEPENDENCIES_RESPONSE_TEMPLATE_KEY = "disableFeature_feature_dependencies_response";
    private static final String DISABLE_FEATURE_RESPONSE_TEMPLATE_KEY = "disableFeature_response";
    private static final String FEATURE_NAME_PARAMETER = "featureName";


    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        String flagKey = (String) commandContext.getParameters().getParameters().get(0);
        FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(flagKey);
        Long serverId = commandContext.getGuild().getIdLong();
        List<FeatureConfig> featureDependencies = disableFeature(feature, serverId);
        if (featureDependencies.isEmpty()) {
            return CompletableFuture.completedFuture(CommandResult.fromSuccess());
        } else {
            List<String> additionalFeatures = featureDependencies
                    .stream()
                    .map(featureDef -> featureDef.getFeature().getKey()).
                            collect(Collectors.toList());
            FeatureSwitchModel model = FeatureSwitchModel
                    .builder()
                    .features(additionalFeatures)
                    .build();
            MessageToSend messageToSend = templateService.renderEmbedTemplate(DISABLE_FEATURE_DEPENDENCIES_RESPONSE_TEMPLATE_KEY,
                    model, serverId);
            return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel()))
                    .thenApply(message -> CommandResult.fromIgnored());
        }
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String featureName = slashCommandParameterService.getCommandOption(FEATURE_NAME_PARAMETER, event, String.class);
        FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(featureName);
        Long serverId = event.getGuild().getIdLong();
        List<FeatureConfig> featureDependencies = disableFeature(feature, serverId);
        if (featureDependencies.isEmpty()) {
            return interactionService.replyEmbed(DISABLE_FEATURE_RESPONSE_TEMPLATE_KEY, event)
                    .thenApply(interactionHook -> CommandResult.fromSuccess());
        } else {
            List<String> additionalFeatures = featureDependencies
                    .stream()
                    .map(featureDef -> featureDef.getFeature().getKey()).
                    collect(Collectors.toList());
            FeatureSwitchModel model = FeatureSwitchModel
                    .builder()
                    .features(additionalFeatures)
                    .build();
            MessageToSend messageToSend = templateService.renderEmbedTemplate(DISABLE_FEATURE_DEPENDENCIES_RESPONSE_TEMPLATE_KEY,
                    model, serverId);
            return interactionService.replyMessageToSend(messageToSend, event)
                    .thenApply(interactionHook -> CommandResult.fromSuccess());
        }
    }

    private List<FeatureConfig> disableFeature(FeatureConfig feature, Long serverId) {
        featureFlagService.disableFeature(feature, serverId);
        List<FeatureConfig> featureDependencies = new ArrayList<>();
        if (feature.getDependantFeatures() != null) {
            AServer server = serverManagementService.loadServer(serverId);
            feature.getDependantFeatures().forEach(featureDisplay -> {
                        if (featureFlagService.isFeatureEnabled(featureDisplay, server)) {
                            featureFlagService.disableFeature(featureDisplay, server);
                            featureDependencies.add(featureDisplay);
                        }
                    }
            );
        }
        return featureDependencies;
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter featureName = Parameter
                .builder()
                .name(FEATURE_NAME_PARAMETER)
                .templated(true)
                .type(String.class)
                .build();
        List<Parameter> parameters = Arrays.asList(featureName);

        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .hasExample(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(CoreSlashCommandNames.FEATURE)
                .commandName("disable")
                .build();

        return CommandConfiguration.builder()
                .name(DISABLE_FEATURE_COMMAND)
                .module(ConfigModuleDefinition.CONFIG)
                .parameters(parameters)
                .async(true)
                .help(helpInfo)
                .templated(true)
                .slashCommandConfig(slashCommandConfig)
                .supportsEmbedException(true)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return CoreFeatureDefinition.CORE_FEATURE;
    }

    @Override
    public List<CommandCondition> getConditions() {
        List<CommandCondition> conditions = super.getConditions();
        conditions.remove(featureEnabledCondition);
        return conditions;
    }
}
