package dev.sheldan.abstracto.core.commands.config.features;

import dev.sheldan.abstracto.core.interaction.slash.CoreSlashCommandNames;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.service.management.FeatureManagementService;
import dev.sheldan.abstracto.core.commands.config.ConfigModuleDefinition;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.template.commands.FeatureModeDisplay;
import dev.sheldan.abstracto.core.models.template.commands.FeatureModesModel;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class FeatureModes extends AbstractConditionableCommand {

    public static final String FEATURE_MODES_RESPONSE_TEMPLATE_KEY = "feature_modes_response";
    private static final String FEATURE_PARAMETER = "feature";
    private static final String FEATURE_MODES_COMMAND = "featureModes";
    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureManagementService featureManagementService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<FeatureModeDisplay> featureModes;
        AServer server = serverManagementService.loadServer(commandContext.getGuild());
        if(commandContext.getParameters().getParameters().isEmpty()) {
            featureModes = featureModeService.getEffectiveFeatureModes(server);
        } else {
            String featureName = (String) commandContext.getParameters().getParameters().get(0);
            FeatureDefinition featureDefinition = featureConfigService.getFeatureEnum(featureName);
            AFeature feature = featureManagementService.getFeature(featureDefinition.getKey());
            featureModes = featureModeService.getEffectiveFeatureModes(server, feature);
        }
        FeatureModesModel model = FeatureModesModel
                .builder()
                .featureModes(featureModes)
                .build();
        return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInMessageChannel(FEATURE_MODES_RESPONSE_TEMPLATE_KEY, model, commandContext.getChannel()))
                .thenApply(aVoid -> CommandResult.fromIgnored());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        List<FeatureModeDisplay> featureModes;
        AServer server = serverManagementService.loadServer(event.getGuild());
        if(!slashCommandParameterService.hasCommandOption(FEATURE_PARAMETER, event)) {
            featureModes = featureModeService.getEffectiveFeatureModes(server);
        } else {
            String featureName = slashCommandParameterService.getCommandOption(FEATURE_PARAMETER, event, String.class);
            FeatureDefinition featureDefinition = featureConfigService.getFeatureEnum(featureName);
            AFeature feature = featureManagementService.getFeature(featureDefinition.getKey());
            featureModes = featureModeService.getEffectiveFeatureModes(server, feature);
        }
        FeatureModesModel model = FeatureModesModel
                .builder()
                .featureModes(featureModes)
                .build();
        return interactionService.replyEmbed(FEATURE_MODES_RESPONSE_TEMPLATE_KEY, model, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter featureName = Parameter
                .builder()
                .name(FEATURE_PARAMETER)
                .type(String.class)
                .optional(true)
                .templated(true)
                .build();
        List<Parameter> parameters = Arrays.asList(featureName);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .defaultPrivilege(SlashCommandPrivilegeLevels.INVITER)
                .rootCommandName(CoreSlashCommandNames.FEATURE)
                .commandName(FEATURE_MODES_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(FEATURE_MODES_COMMAND)
                .module(ConfigModuleDefinition.CONFIG)
                .parameters(parameters)
                .templated(true)
                .supportsEmbedException(true)
                .slashCommandConfig(slashCommandConfig)
                .help(helpInfo)
                .async(true)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return CoreFeatureDefinition.CORE_FEATURE;
    }
}
