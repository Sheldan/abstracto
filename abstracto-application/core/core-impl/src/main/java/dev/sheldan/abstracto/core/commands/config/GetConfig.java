package dev.sheldan.abstracto.core.commands.config;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.ConfigurationKeyNotFoundException;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.CoreSlashCommandNames;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandAutoCompleteService;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.models.database.AConfig;
import dev.sheldan.abstracto.core.models.property.SystemConfigProperty;
import dev.sheldan.abstracto.core.models.template.commands.GetConfigModel;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.PaginatorService;
import dev.sheldan.abstracto.core.service.management.ConfigManagementService;
import dev.sheldan.abstracto.core.service.management.DefaultConfigManagementService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GetConfig extends AbstractConditionableCommand {

    private static final String GET_CONFIG_COMMAND = "getConfig";
    private static final String KEY_PARAMETER = "key";
    private static final String FEATURE_PARAMETER = "feature";

    private static final String GET_CONFIG_RESPONSE_TEMPLATE_KEY = "getConfig_response";
    private static final String NO_CONFIGS_TEMPLATE_KEY = "getConfig_no_configs_found";

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private SlashCommandAutoCompleteService slashCommandAutoCompleteService;

    @Autowired
    private DefaultConfigManagementService defaultConfigManagementService;

    @Autowired
    private ConfigManagementService configManagementService;

    @Autowired
    private PaginatorService paginatorService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Long serverId = event.getGuild().getIdLong();
        List<GetConfigModel.ConfigValue> configValues;
        if(slashCommandParameterService.hasCommandOption(KEY_PARAMETER, event)) {
            String key = slashCommandParameterService.getCommandOption(KEY_PARAMETER, event, String.class);
            if(!defaultConfigManagementService.configKeyExists(key)) {
                throw new ConfigurationKeyNotFoundException(key);
            }

            GetConfigModel.ConfigValue.ConfigValueBuilder configValueBuilder = GetConfigModel.ConfigValue.builder();
            if(configManagementService.configExists(serverId, key)) {
                AConfig aConfig = configManagementService.loadConfig(serverId, key);
                configValueBuilder
                    .doubleValue(aConfig.getDoubleValue())
                    .hasConcreateValue(true)
                    .longValue(aConfig.getLongValue())
                    .stringValue(aConfig.getStringValue());
            }
            SystemConfigProperty defaultConfig = defaultConfigManagementService.getDefaultConfig(key);
            GetConfigModel.ConfigValue.ConfigValueBuilder defaultConfigValueBuilder = GetConfigModel
                .ConfigValue
                .builder()
                .stringValue(defaultConfig.getStringValue())
                .doubleValue(defaultConfig.getDoubleValue())
                .longValue(defaultConfig.getLongValue())
                .key(defaultConfig.getName());
            configValueBuilder
                .key(defaultConfig.getName())
                .defaultValue(defaultConfigValueBuilder.build());
            configValues = List.of(configValueBuilder.build());

        } else if(slashCommandParameterService.hasCommandOption(FEATURE_PARAMETER, event)) {
            String featureKey = slashCommandParameterService.getCommandOption(FEATURE_PARAMETER, event, String.class);
            FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(featureKey);
            List<String> configKeys = feature.getRequiredSystemConfigKeys();
            configValues = getConfigValuesForKeys(serverId, configKeys);
        } else {
            List<String> configKeys = defaultConfigManagementService.getConfigKeys();
            configValues = getConfigValuesForKeys(serverId, configKeys);
        }

        if(configValues.isEmpty()) {
            return interactionService.replyEmbed(NO_CONFIGS_TEMPLATE_KEY, new Object() , event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
        }

        configValues = new ArrayList<>(configValues);
        configValues.sort(Comparator.comparing(GetConfigModel.ConfigValue::getKey));
        GetConfigModel model = GetConfigModel.builder()
            .values(configValues)
            .build();

        return paginatorService.createPaginatorFromTemplate(GET_CONFIG_RESPONSE_TEMPLATE_KEY, model, event)
            .thenApply(unused -> CommandResult.fromSuccess());
    }

    private List<GetConfigModel.ConfigValue> getConfigValuesForKeys(Long serverId, List<String> configKeys) {
        Map<String, AConfig> allExistingConfigs = configManagementService
            .loadForServer(serverId)
            .stream()
            .collect(Collectors.toMap(aConfig -> aConfig.getName().toLowerCase(), Function.identity()));
        return configKeys.stream().map(key -> {
            GetConfigModel.ConfigValue.ConfigValueBuilder configValueBuilder = GetConfigModel.ConfigValue.builder();
            if(allExistingConfigs.containsKey(key.toLowerCase())) {
                AConfig aConfig = allExistingConfigs.get(key.toLowerCase());
                configValueBuilder
                    .doubleValue(aConfig.getDoubleValue())
                    .hasConcreateValue(true)
                    .longValue(aConfig.getLongValue())
                    .stringValue(aConfig.getStringValue());
            }
            SystemConfigProperty defaultConfig = defaultConfigManagementService.getDefaultConfig(key);
            GetConfigModel.ConfigValue.ConfigValueBuilder defaultConfigValueBuilder = GetConfigModel
                .ConfigValue
                .builder()
                .stringValue(defaultConfig.getStringValue())
                .doubleValue(defaultConfig.getDoubleValue())
                .longValue(defaultConfig.getLongValue())
                .key(defaultConfig.getName());
            configValueBuilder
                .key(defaultConfig.getName())
                .defaultValue(defaultConfigValueBuilder.build());
            return configValueBuilder.build();
        }).toList();
    }

    @Override
    public List<String> performAutoComplete(CommandAutoCompleteInteractionEvent event) {
        String input = event.getFocusedOption().getValue().toLowerCase();
        if(slashCommandAutoCompleteService.matchesParameter(event.getFocusedOption(), KEY_PARAMETER)) {
            return defaultConfigManagementService
                .getConfigKeys()
                .stream()
                .map(String::toLowerCase)
                .filter(key -> key.startsWith(input))
                .toList();
        } else if(slashCommandAutoCompleteService.matchesParameter(event.getFocusedOption(), FEATURE_PARAMETER)) {
            return featureConfigService.getAllFeatures()
                .stream()
                .map(String::toLowerCase)
                .filter(lowerCase -> lowerCase.startsWith(input))
                .toList();
        }
        return new ArrayList<>();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter keyToGet = Parameter
            .builder()
            .name(KEY_PARAMETER)
            .type(String.class)
            .supportsAutoComplete(true)
            .templated(true)
            .optional(true)
            .build();
        Parameter featureToGet = Parameter
            .builder()
            .name(FEATURE_PARAMETER)
            .type(String.class)
            .supportsAutoComplete(true)
            .templated(true)
            .optional(true)
            .build();
        List<Parameter> parameters = Arrays.asList(keyToGet, featureToGet);
        HelpInfo helpInfo = HelpInfo
            .builder()
            .templated(true)
            .hasExample(true)
            .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
            .builder()
            .enabled(true)
            .defaultPrivilege(SlashCommandPrivilegeLevels.INVITER)
            .rootCommandName(CoreSlashCommandNames.CONFIG)
            .commandName("get")
            .build();

        return CommandConfiguration.builder()
            .name(GET_CONFIG_COMMAND)
            .module(ConfigModuleDefinition.CONFIG)
            .parameters(parameters)
            .slashCommandOnly(true)
            .slashCommandConfig(slashCommandConfig)
            .templated(true)
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
