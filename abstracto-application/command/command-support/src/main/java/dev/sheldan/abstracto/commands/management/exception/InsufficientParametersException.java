package dev.sheldan.abstracto.commands.management.exception;

import dev.sheldan.abstracto.command.TemplatedException;
import dev.sheldan.abstracto.command.Command;
import lombok.Getter;

import java.util.HashMap;

@Getter
public class InsufficientParametersException extends RuntimeException implements TemplatedException {

    private Command command;
    private String parameterName;

    public InsufficientParametersException(String s, Command command, String parameterName) {
        super(s);
        this.command = command;
        this.parameterName = parameterName;
    }

    @Override
    public String getTemplateName() {
        return "insufficient_parameters";
    }

    @Override
    public Object getTemplateModel() {
        HashMap<String, Object> model = new HashMap<>();
        model.put("parameterName", parameterName);
        return model;
    }
}
