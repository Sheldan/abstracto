package dev.sheldan.abstracto.core.command.condition;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public abstract class AbstractConditionableCommand implements ConditionalCommand {

    @Autowired
    protected FeatureEnabledCondition featureEnabledCondition;

    @Autowired
    protected CommandDisabledCondition commandDisabledCondition;

    @Autowired
    protected CommandDisallowedCondition commandDisallowedCondition;

    @Autowired
    protected ImmuneUserCondition immuneUserCondition;

    @Autowired
    private FeatureModeCondition featureModeCondition;


    @Override
    public List<CommandCondition> getConditions() {
        return new ArrayList<>(Arrays.asList(featureEnabledCondition, commandDisabledCondition, commandDisallowedCondition, featureModeCondition));
    }
}
