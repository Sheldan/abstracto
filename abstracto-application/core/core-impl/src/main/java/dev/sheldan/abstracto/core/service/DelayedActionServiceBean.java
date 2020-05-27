package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.interactive.DelayedAction;
import dev.sheldan.abstracto.core.interactive.DelayedActionConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DelayedActionServiceBean implements DelayedActionService {

    @Autowired
    private List<DelayedAction> delayedActions;

    @Override
    public void executeDelayedActions(List<DelayedActionConfig> delayedActionConfigList) {
        delayedActionConfigList.forEach(delayedActionConfig ->
            delayedActions.stream()
                    .filter(delayedAction -> delayedAction.handles(delayedActionConfig))
                    .findFirst()
                    .ifPresent(delayedAction -> delayedAction.execute(delayedActionConfig))
        );
    }
}
