package dev.sheldan.abstracto.moderation.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;

public class WarnNotFoundException extends AbstractoTemplatableException {
    @Override
    public String getTemplateName() {
        return "warn_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
