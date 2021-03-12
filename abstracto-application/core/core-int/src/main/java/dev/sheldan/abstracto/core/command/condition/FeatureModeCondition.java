package dev.sheldan.abstracto.core.command.condition;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.condition.detail.IncorrectFeatureModeConditionDetail;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FeatureModeCondition implements CommandCondition {

    @Autowired
    private FeatureModeService modeService;

    @Override
    public ConditionResult shouldExecute(CommandContext context, Command command) {
        if(!command.getFeatureModeLimitations().isEmpty()){
            FeatureDefinition feature = command.getFeature();
            if(feature != null) {
                for (FeatureMode featureModeLimitation : command.getFeatureModeLimitations()) {
                    if(modeService.featureModeActive(feature, context.getUserInitiatedContext().getGuild().getIdLong(), featureModeLimitation)) {
                        return ConditionResult.builder().result(true).build();
                    }
                }
                return ConditionResult
                        .builder()
                        .result(false)
                        .conditionDetail(new IncorrectFeatureModeConditionDetail(feature, command.getFeatureModeLimitations()))
                        .build();
            }
        }

        return ConditionResult.builder().result(true).build();
    }
}
