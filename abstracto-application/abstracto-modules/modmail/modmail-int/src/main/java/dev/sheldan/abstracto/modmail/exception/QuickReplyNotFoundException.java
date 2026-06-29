package dev.sheldan.abstracto.modmail.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;

public class QuickReplyNotFoundException extends AbstractoTemplatableException {
    @Override
    public String getTemplateName() {
        return "quick_reply_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
