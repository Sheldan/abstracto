package dev.sheldan.abstracto.core.command.condition;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.exception.IncorrectFeatureModeException;
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
            FeatureEnum feature = command.getFeature();
            if(feature != null) {
                for (FeatureMode featureModeLimitation : command.getFeatureModeLimitations()) {
                    if(modeService.featureModeActive(feature, context.getUserInitiatedContext().getServer(), featureModeLimitation)) {
                        return ConditionResult.builder().result(true).build();
                    }
                }
                throw new IncorrectFeatureModeException(command, feature, command.getFeatureModeLimitations());
            }
        }

        return ConditionResult.builder().result(true).build();
    }
}
