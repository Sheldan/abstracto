package dev.sheldan.abstracto.core.command.condition;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.models.exception.IncorrectFeatureModeMessage;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.models.database.AFeatureMode;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.templating.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FeatureModeCondition implements CommandCondition {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private FeatureModeService modeService;

    @Override
    public ConditionResult shouldExecute(CommandContext context, Command command) {
        String reason = "";
        boolean featureModeFits = true;
        if(!command.getFeatureModeLimitations().isEmpty()){
            FeatureEnum feature = command.getFeature();
            if(feature != null) {
                AFeatureMode featureMode = modeService.getFeatureMode(feature, context.getUserInitiatedContext().getServer());
                featureModeFits = command.getFeatureModeLimitations().stream().anyMatch(featureMode1 -> featureMode1.getKey().equalsIgnoreCase(featureMode.getMode()));
                if(!featureModeFits) {
                    IncorrectFeatureModeMessage featureDisabledMessage = IncorrectFeatureModeMessage
                            .builder()
                            .featureConfig(featureConfigService.getFeatureDisplayForFeature(feature))
                            .command(command)
                            .build();
                    reason = templateService.renderTemplate("feature_mode_not_correct_message", featureDisabledMessage);
                }
            }
        }

        return ConditionResult.builder().reason(reason).result(featureModeFits).build();
    }
}
