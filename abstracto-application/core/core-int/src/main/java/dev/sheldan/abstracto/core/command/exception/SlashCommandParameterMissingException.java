package dev.sheldan.abstracto.core.command.exception;

import dev.sheldan.abstracto.core.command.model.exception.SlashCommandParameterMissingModel;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

public class SlashCommandParameterMissingException extends AbstractoRunTimeException implements Templatable {

    private final SlashCommandParameterMissingModel model;

    public SlashCommandParameterMissingException(String parameterName) {
        this.model = SlashCommandParameterMissingModel
                .builder()
                .parameterName(parameterName)
                .build();
    }

    @Override
    public String getTemplateName() {
        return "slash_command_parameter_missing_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
