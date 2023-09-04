package dev.sheldan.abstracto.customcommand.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;

public class CustomCommandExistsException extends AbstractoTemplatableException {
    @Override
    public String getTemplateName() {
        return "custom_command_exists_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
