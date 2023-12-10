package dev.sheldan.abstracto.giveaway.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;

public class GiveawayNotFoundException extends AbstractoTemplatableException {
    @Override
    public String getTemplateName() {
        return "giveaway_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
