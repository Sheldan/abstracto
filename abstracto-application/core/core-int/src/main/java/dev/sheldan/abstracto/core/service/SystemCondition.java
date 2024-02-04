package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.ConditionContext;
import dev.sheldan.abstracto.core.models.ConditionContextInstance;

public interface SystemCondition {
    Result checkCondition(ConditionContextInstance conditionContext);
    String getConditionName();
    ConditionContext getExpectedContext();

    enum Result {
        SUCCESSFUL, FAILED, IGNORED;

        public static Result fromBoolean(boolean value) {
            return value ? SUCCESSFUL : FAILED;
        }

        public static boolean consideredSuccessful(Result result) {
            return result == Result.SUCCESSFUL || result == Result.IGNORED;
        }

        public static boolean isSuccessful(Result result) {
            return result == Result.SUCCESSFUL;
        }
    }
}
