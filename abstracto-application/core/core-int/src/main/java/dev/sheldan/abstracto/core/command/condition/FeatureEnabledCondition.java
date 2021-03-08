package dev.sheldan.abstracto.core.command.condition;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.condition.detail.FeatureDisabledConditionDetail;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import lombok.extern.slf4j.Slf4j;
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
        FeatureEnum feature = command.getFeature();
        boolean featureFlagValue = true;
        if(feature != null) {
            featureFlagValue = featureFlagService.getFeatureFlagValue(feature, context.getGuild().getIdLong());
            if(!featureFlagValue) {
                log.trace("Feature {} is disabled, disallows command {} to be executed in guild {}.", feature.getKey(), command.getConfiguration().getName(), context.getGuild().getId());
                FeatureDisabledConditionDetail exception = new FeatureDisabledConditionDetail(featureConfigService.getFeatureDisplayForFeature(command.getFeature()));
                return ConditionResult.builder().result(false).conditionDetail(exception).build();
            }
        }
        return ConditionResult.builder().result(true).build();
    }
}
