package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.exception.InvalidConditionParametersException;
import dev.sheldan.abstracto.core.models.ConditionContextInstance;
import dev.sheldan.abstracto.core.models.ConditionContextVariable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class ConditionServiceBean implements ConditionService {

    @Autowired(required = false)
    private List<SystemCondition> conditionList;

    @Override
    public boolean checkConditions(ConditionContextInstance context) {
        if(conditionList == null ||  conditionList.isEmpty()) {
            return true;
        }
        Optional<SystemCondition> matchingCondition = conditionList
                .stream()
                .filter(systemCondition -> systemCondition.getConditionName().equalsIgnoreCase(context.getConditionName()))
                .findAny();
        log.debug("Checking condition {}.", context.getConditionName());
        return matchingCondition.map(systemCondition -> {
            verifyConditionContext(context, systemCondition);
            boolean result = systemCondition.checkCondition(context);
            log.debug("Condition resulted in {}.", result);
            return result;
        }).orElse(true);
    }

    private void verifyConditionContext(ConditionContextInstance contextInstance, SystemCondition condition) {
        for (ConditionContextVariable conditionContextVariable : condition.getExpectedContext().getRequiredVariables()) {
            Map<String, Object> providedParameters = contextInstance.getParameters();
            if(!providedParameters.containsKey(conditionContextVariable.getName())) {
                throw new InvalidConditionParametersException(String.format("Variable %s was not present", conditionContextVariable.getName()));
            }
            Class expectedType = conditionContextVariable.getType();
            Object providedParameter = providedParameters.get(conditionContextVariable.getName());
            if(!expectedType.isInstance(providedParameter)) {
                throw new InvalidConditionParametersException(String.format("Variable %s was of type %s instead of %s.",
                        conditionContextVariable.getName(), providedParameter.getClass(), expectedType));
            }
        }
    }
}
