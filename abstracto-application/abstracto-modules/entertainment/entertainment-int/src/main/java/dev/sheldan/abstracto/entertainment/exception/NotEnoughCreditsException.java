package dev.sheldan.abstracto.entertainment.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;

public class NotEnoughCreditsException extends AbstractoTemplatableException {
    @Override
    public String getTemplateName() {
        return "not_enough_wealth_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
