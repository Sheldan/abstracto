package dev.sheldan.abstracto.modmail.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.templating.Templatable;

public class AlreadySubscribedException extends AbstractoRunTimeException implements Templatable {
    public AlreadySubscribedException(String message) {
        super(message);
    }

    @Override
    public String getTemplateName() {
        return "modmail_already_subscribed_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
