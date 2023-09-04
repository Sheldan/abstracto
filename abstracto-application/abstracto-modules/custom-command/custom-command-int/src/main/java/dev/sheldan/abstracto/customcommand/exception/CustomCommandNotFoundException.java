package dev.sheldan.abstracto.customcommand.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;

public class CustomCommandNotFoundException extends AbstractoTemplatableException {
    @Override
    public String getTemplateName() {
        return "custom_command_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
