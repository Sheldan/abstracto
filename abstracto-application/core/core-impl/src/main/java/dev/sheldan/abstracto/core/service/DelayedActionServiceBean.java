package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.interactive.DelayedAction;
import dev.sheldan.abstracto.core.interactive.DelayedActionConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class DelayedActionServiceBean implements DelayedActionService {

    @Autowired
    private List<DelayedAction> delayedActions;

    @Override
    public void executeDelayedActions(List<DelayedActionConfig> delayedActionConfigList) {
        delayedActionConfigList.forEach(delayedActionConfig -> {
            log.debug("Executing delayed action {}.", delayedActionConfig.getClass().getSimpleName());
            delayedActions.stream()
                    .filter(delayedAction -> delayedAction.handles(delayedActionConfig))
                    .findFirst()
                    .ifPresent(delayedAction -> delayedAction.execute(delayedActionConfig));
        });
    }
}
