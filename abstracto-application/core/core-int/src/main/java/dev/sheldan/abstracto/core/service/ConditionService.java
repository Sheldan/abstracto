package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.ConditionContextInstance;

public interface ConditionService {
    SystemCondition.Result checkConditions(ConditionContextInstance context);
}
