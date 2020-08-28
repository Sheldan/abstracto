package dev.sheldan.abstracto.core.command.condition;

import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.exception.IncorrectParameterException;
import dev.sheldan.abstracto.core.command.exception.InsufficientParametersException;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
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
    protected CommandDisallowedCondition commandDisallowedCondition;

    @Autowired
    protected ImmuneUserCondition immuneUserCondition;

    @Autowired
    private FeatureModeCondition featureModeCondition;


    @Override
    public List<CommandCondition> getConditions() {
        return new ArrayList<>(Arrays.asList(featureEnabledCondition, commandDisabledCondition, commandDisallowedCondition, featureModeCondition));
    }

    protected void checkParameters(CommandContext context) {
        List<Parameter> parameters = getConfiguration().getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            Parameter parameter = parameters.get(i);
            Class desiredType = parameter.getType();
            if(!parameter.isOptional()) {
                checkMandatoryExp(context, i, parameter, desiredType);
            } else {
                checkOptionalParameter(context, i, parameter, desiredType);
            }
        }
    }

    private void checkOptionalParameter(CommandContext context, int i, Parameter parameter, Class desiredType) {
        if(context.getParameters() != null && context.getParameters().getParameters() != null && context.getParameters().getParameters().size() >= i) {
            boolean parameterIsPresent = i < context.getParameters().getParameters().size();
            if(parameterIsPresent && !desiredType.isInstance(context.getParameters().getParameters().get(i))) {
                throw new IncorrectParameterException(this, desiredType, parameter.getName());
            }
        }
    }

    private void checkMandatoryExp(CommandContext context, int i, Parameter parameter, Class desiredType) {
        if(context.getParameters() == null || context.getParameters().getParameters() == null || context.getParameters().getParameters().isEmpty() || i >= context.getParameters().getParameters().size()) {
            throw new InsufficientParametersException(this, parameter.getName());
        }
        if(!desiredType.isInstance(context.getParameters().getParameters().get(i))) {
            throw new IncorrectParameterException(this, desiredType, parameter.getName());
        }
    }
}
