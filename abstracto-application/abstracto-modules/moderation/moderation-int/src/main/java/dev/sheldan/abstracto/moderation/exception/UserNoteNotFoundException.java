package dev.sheldan.abstracto.moderation.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;

public class UserNoteNotFoundException extends AbstractoTemplatableException {
    @Override
    public String getTemplateName() {
        return "user_note_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
