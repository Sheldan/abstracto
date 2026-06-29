package dev.sheldan.abstracto.modmail.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;

public class QuickReplyExistsException extends AbstractoTemplatableException {
    @Override
    public String getTemplateName() {
        return "quick_reply_exists_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
