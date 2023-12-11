package dev.sheldan.abstracto.entertainment.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;

public class AlreadyPressedFException extends AbstractoTemplatableException {
    @Override
    public String getTemplateName() {
        return "already_pressed_f_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
