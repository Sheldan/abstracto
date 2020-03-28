package dev.sheldan.abstracto.commands.management.exception;

import dev.sheldan.abstracto.command.Command;
import dev.sheldan.abstracto.command.TemplatedException;

import java.util.HashMap;

public class ParameterTooLongException extends RuntimeException implements TemplatedException {


    private Command command;
    private String parameterName;
    private Integer actualLength;
    private Integer maximumLength;

    public ParameterTooLongException(String s, Command command, String parameterName, Integer actualLength, Integer maximumLength) {
        super(s);
        this.command = command;
        this.parameterName = parameterName;
        this.actualLength = actualLength;
        this.maximumLength = maximumLength;
    }

    @Override
    public String getTemplateName() {
        return "parameter_too_long";
    }

    @Override
    public Object getTemplateModel() {
        HashMap<String, Object> model = new HashMap<>();
        model.put("parameterName", parameterName);
        model.put("actualLength", actualLength);
        model.put("maximumLength", maximumLength);
        model.put("command", command);
        return model;
    }
}
