package dev.sheldan.abstracto.core.command.condition;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.condition.detail.FeatureDisabledConditionDetail;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FeatureEnabledCondition implements CommandCondition {

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Override
    public ConditionResult shouldExecute(CommandContext context, Command command) {
        Long serverId = context.getGuild().getIdLong();
        return evaluateFeatureCondition(command, serverId);
    }

    @Override
    public ConditionResult shouldExecute(SlashCommandInteractionEvent slashCommandInteractionEvent, Command command) {
        Long serverId = slashCommandInteractionEvent.getGuild().getIdLong();
        return evaluateFeatureCondition(command, serverId);
    }

    private ConditionResult evaluateFeatureCondition(Command command, Long serverId) {
        FeatureDefinition feature = command.getFeature();
        boolean featureFlagValue;
        if(feature != null) {
            featureFlagValue = featureFlagService.getFeatureFlagValue(feature, serverId);
            if(!featureFlagValue) {
                log.debug("Feature {} is disabled, disallows command {} to be executed in guild {}.", feature.getKey(), command.getConfiguration().getName(), serverId);
                FeatureDisabledConditionDetail exception = new FeatureDisabledConditionDetail(featureConfigService.getFeatureDisplayForFeature(command.getFeature()));
                return ConditionResult
                        .builder()
                        .result(false)
                        .conditionDetail(exception)
                        .build();
            }
        }
        return ConditionResult
                .builder()
                .result(true)
                .build();
    }

    @Override
    public boolean supportsSlashCommands() {
        return true;
    }
}
