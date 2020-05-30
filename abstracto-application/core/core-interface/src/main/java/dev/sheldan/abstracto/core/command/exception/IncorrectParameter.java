package dev.sheldan.abstracto.core.command.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.templating.Templatable;

import java.util.HashMap;

public class IncorrectParameter extends AbstractoRunTimeException implements Templatable {

    private final transient Command command;
    private final String parameterName;
    private final Class clazz;

    public IncorrectParameter(Command command, Class expected, String parameterName) {
        super("");
        this.command = command;
        this.parameterName = parameterName;
        this.clazz = expected;
    }

    @Override
    public String getTemplateName() {
        return "incorrect_parameters";
    }

    @Override
    public Object getTemplateModel() {
        HashMap<String, Object> model = new HashMap<>();
        model.put("parameterName", parameterName);
        model.put("class", this.clazz);
        return model;
    }
}
