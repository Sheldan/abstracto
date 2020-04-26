package dev.sheldan.abstracto.core.command.condition;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.models.FeatureDisabledMessage;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.management.FeatureFlagManagementService;
import dev.sheldan.abstracto.templating.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FeatureEnabledCondition implements CommandCondition {

    @Autowired
    private FeatureFlagManagementService featureFlagManagementService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Override
    public ConditionResult shouldExecute(CommandContext context, Command command) {
        FeatureEnum feature = command.getFeature();
        boolean featureFlagValue = true;
        String reason = "";
        if(feature != null) {
            featureFlagValue = featureFlagManagementService.getFeatureFlagValue(feature, context.getGuild().getIdLong());
            FeatureDisabledMessage featureDisabledMessage = FeatureDisabledMessage
                    .builder()
                    .featureDisplay(featureFlagService.getFeatureDisplayforFeature(feature))
                    .build();
            reason = templateService.renderTemplate("feature_disabled_message", featureDisabledMessage);
        }
        return ConditionResult.builder().reason(reason).result(featureFlagValue).build();
    }
}
