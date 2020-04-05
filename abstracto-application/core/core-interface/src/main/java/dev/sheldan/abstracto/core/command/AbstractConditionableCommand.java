package dev.sheldan.abstracto.core.command;

import dev.sheldan.abstracto.core.service.ChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public abstract class AbstractConditionableCommand implements ConditionalCommand {

    @Autowired
    private FeatureEnabledCondition featureEnabledCondition;

    @Autowired
    private CommandDisabledCondition commandDisabledCondition;

    @Autowired
    protected ChannelService channelService;


    @Override
    public List<CommandCondition> getConditions() {
        return Arrays.asList(featureEnabledCondition, commandDisabledCondition);
    }
}
