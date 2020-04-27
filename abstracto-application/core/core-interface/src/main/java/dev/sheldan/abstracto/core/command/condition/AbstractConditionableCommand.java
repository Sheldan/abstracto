package dev.sheldan.abstracto.core.command.condition;

import dev.sheldan.abstracto.core.service.ChannelService;
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
    protected ChannelService channelService;

    @Autowired
    protected CommandDisallowedCondition commandDisallowedCondition;

    @Autowired
    protected ImmuneUserCondition immuneUserCondition;


    @Override
    public List<CommandCondition> getConditions() {
        return new ArrayList<>(Arrays.asList(featureEnabledCondition, commandDisabledCondition, commandDisallowedCondition));
    }
}
