package dev.sheldan.abstracto.core.command.exception;

import dev.sheldan.abstracto.core.command.models.exception.CommandParameterKeyValueWrongTypeExceptionModel;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.templating.Templatable;

import java.util.List;

public class CommandParameterKeyValueWrongTypeException extends AbstractoRunTimeException implements Templatable {

    private CommandParameterKeyValueWrongTypeExceptionModel model;

    public CommandParameterKeyValueWrongTypeException(List<String> expectedValues) {
        super("Command parameter value did not have expected values present");
        this.model = CommandParameterKeyValueWrongTypeExceptionModel.builder().expectedValues(expectedValues).build();
    }

    @Override
    public String getTemplateName() {
        return "command_parameter_value_wrong_type_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
