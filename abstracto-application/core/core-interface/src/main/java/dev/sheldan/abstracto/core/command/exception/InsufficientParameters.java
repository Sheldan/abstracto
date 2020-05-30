package dev.sheldan.abstracto.core.command.exception;

import dev.sheldan.abstracto.templating.Templatable;
import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import lombok.Getter;

import java.util.HashMap;

@Getter
public class InsufficientParameters extends AbstractoRunTimeException implements Templatable {

    private final transient Command command;
    private final String parameterName;

    public InsufficientParameters(Command command, String parameterName) {
        super("");
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
