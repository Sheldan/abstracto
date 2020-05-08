package dev.sheldan.abstracto.modmail.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.templating.Templatable;

public class NotSubscribedException extends AbstractoRunTimeException implements Templatable {
    public NotSubscribedException(String message) {
        super(message);
    }

    @Override
    public String getTemplateName() {
        return "modmail_not_subscribed_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
