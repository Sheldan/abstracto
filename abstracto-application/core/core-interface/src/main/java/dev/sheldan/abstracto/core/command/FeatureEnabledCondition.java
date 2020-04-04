package dev.sheldan.abstracto.core.command;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.service.management.FeatureFlagManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FeatureEnabledCondition implements CommandCondition {

    @Autowired
    private FeatureFlagManagementService featureFlagManagementService;

    @Override
    public boolean shouldExecute(CommandContext context, Command command) {
        String featureName = command.getFeature();
        if(featureName != null) {
            return featureFlagManagementService.getFeatureFlagValue(featureName, context.getGuild().getIdLong());
        }
        return false;
    }
}
