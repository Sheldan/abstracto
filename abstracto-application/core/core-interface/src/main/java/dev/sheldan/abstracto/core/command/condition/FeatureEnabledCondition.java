package dev.sheldan.abstracto.core.command.condition;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.exception.FeatureDisabledException;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.templating.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FeatureEnabledCondition implements CommandCondition {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Override
    public ConditionResult shouldExecute(CommandContext context, Command command) {
        FeatureEnum feature = command.getFeature();
        boolean featureFlagValue = true;
        String reason = "";
        if(feature != null) {
            featureFlagValue = featureFlagService.getFeatureFlagValue(feature, context.getGuild().getIdLong());
            if(!featureFlagValue) {
                throw new FeatureDisabledException(featureConfigService.getFeatureDisplayForFeature(feature));
            }
        }
        return ConditionResult.builder().reason(reason).result(featureFlagValue).build();
    }
}
