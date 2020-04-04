package dev.sheldan.abstracto.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public abstract class AbstractFeatureFlaggedCommand implements ConditionalCommand {

    @Autowired
    private FeatureEnabledCondition featureEnabledCondition;

    @Override
    public List<CommandCondition> getConditions() {
        return Arrays.asList(featureEnabledCondition);
    }
}
