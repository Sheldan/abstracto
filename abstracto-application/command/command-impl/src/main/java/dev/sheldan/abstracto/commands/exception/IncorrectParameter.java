package dev.sheldan.abstracto.commands.exception;

import dev.sheldan.abstracto.command.Command;
import dev.sheldan.abstracto.command.Templatable;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;

import java.util.HashMap;

public class IncorrectParameter extends AbstractoRunTimeException implements Templatable {

    private Command command;
    private String parameterName;
    private Class clazz;

    public IncorrectParameter(String s, Command command, Class expected, String parameterName) {
        super(s);
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
