package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.interactive.DelayedActionConfig;

import java.util.List;

public interface DelayedActionService {
    void executeDelayedActions(List<DelayedActionConfig> delayedActionConfigList);
}
