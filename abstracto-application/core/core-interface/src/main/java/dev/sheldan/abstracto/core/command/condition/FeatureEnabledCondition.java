package dev.sheldan.abstracto.core.command.condition;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.service.management.FeatureFlagManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FeatureEnabledCondition implements CommandCondition {

    @Autowired
    private FeatureFlagManagementService featureFlagManagementService;

    @Override
    public ConditionResult shouldExecute(CommandContext context, Command command) {
        String featureName = command.getFeature();
        boolean featureFlagValue = false;
        String reason = "";
        if(featureName != null) {
            featureFlagValue = featureFlagManagementService.getFeatureFlagValue(featureName, context.getGuild().getIdLong());
            reason = "Feature has been disabled.";
        }
        return ConditionResult.builder().reason(reason).result(featureFlagValue).build();
    }
}
