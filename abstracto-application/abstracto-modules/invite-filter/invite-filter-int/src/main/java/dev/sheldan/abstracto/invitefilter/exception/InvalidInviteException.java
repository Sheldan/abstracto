package dev.sheldan.abstracto.invitefilter.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

public class InvalidInviteException extends AbstractoRunTimeException implements Templatable {

    public InvalidInviteException(String message) {
        super(message);
    }

    @Override
    public String getTemplateName() {
        return "invalid_invite_link_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
