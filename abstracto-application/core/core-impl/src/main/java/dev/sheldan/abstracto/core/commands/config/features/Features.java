package dev.sheldan.abstracto.core.commands.config.features;

import dev.sheldan.abstracto.core.interaction.slash.CoreSlashCommandNames;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.commands.config.ConfigModuleDefinition;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.converter.FeatureFlagConverter;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.models.database.AFeatureFlag;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.property.FeatureFlagProperty;
import dev.sheldan.abstracto.core.models.template.commands.FeaturesModel;
import dev.sheldan.abstracto.core.service.management.DefaultFeatureFlagManagementService;
import dev.sheldan.abstracto.core.service.management.FeatureFlagManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class Features extends AbstractConditionableCommand {

    private static final String FEATURES_COMMAND = "features";
    @Autowired
    private FeatureFlagManagementService featureFlagManagementService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private FeatureFlagConverter featureFlagConverter;

    @Autowired
    private DefaultFeatureFlagManagementService defaultFeatureFlagManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        MessageToSend messageToSend = getMessageToSend(event.getGuild().getIdLong());
        return interactionService.replyMessageToSend(messageToSend, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    private MessageToSend getMessageToSend(Long serverId) {
        AServer server = serverManagementService.loadServer(serverId);
        List<AFeatureFlag> features = featureFlagManagementService.getFeatureFlagsOfServer(server);
        List<FeatureFlagProperty> defaultFeatureFlagProperties = defaultFeatureFlagManagementService.getAllDefaultFeatureFlags();
        features.sort(Comparator.comparing(o -> o.getFeature().getKey()));
        defaultFeatureFlagProperties = defaultFeatureFlagProperties
                .stream()
                .filter(featureFlagProperty ->
                        features
                                .stream()
                                .noneMatch(aFeatureFlag ->
                                        aFeatureFlag.getFeature().getKey()
                                                .equals(featureFlagProperty.getFeatureName())))
                .collect(Collectors.toList());
        defaultFeatureFlagProperties.sort(Comparator.comparing(FeatureFlagProperty::getFeatureName));
        FeaturesModel featuresModel = FeaturesModel
                .builder()
                .features(featureFlagConverter.fromFeatureFlags(features))
                .defaultFeatures(featureFlagConverter.fromFeatureFlagProperties(defaultFeatureFlagProperties))
                .build();
        return templateService.renderEmbedTemplate("features_response", featuresModel, serverId);
    }

    @Override
    public CommandConfiguration getConfiguration() {
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .defaultPrivilege(SlashCommandPrivilegeLevels.INVITER)
                .rootCommandName(CoreSlashCommandNames.FEATURE)
                .commandName("list")
                .build();

        return CommandConfiguration.builder()
                .name(FEATURES_COMMAND)
                .module(ConfigModuleDefinition.CONFIG)
                .templated(true)
                .async(true)
                .slashCommandOnly(true)
                .slashCommandConfig(slashCommandConfig)
                .supportsEmbedException(true)
                .help(helpInfo)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return CoreFeatureDefinition.CORE_FEATURE;
    }
}
