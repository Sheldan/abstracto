package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.ConditionContext;
import dev.sheldan.abstracto.core.models.ConditionContextInstance;

public interface SystemCondition {
    boolean checkCondition(ConditionContextInstance conditionContext);
    String getConditionName();
    ConditionContext getExpectedContext();
}
