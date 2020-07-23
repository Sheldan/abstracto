package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.ConditionContextInstance;

public interface ConditionService {
    boolean checkConditions(ConditionContextInstance context);
}
