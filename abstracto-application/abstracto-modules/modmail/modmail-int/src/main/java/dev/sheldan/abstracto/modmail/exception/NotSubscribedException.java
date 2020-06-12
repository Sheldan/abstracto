package dev.sheldan.abstracto.modmail.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.templating.Templatable;

/**
 * This exception is thrown when you try to unsubscribe from a mod mail thread, to which you are not subscribed to
 */
public class NotSubscribedException extends AbstractoRunTimeException implements Templatable {
    public NotSubscribedException() {
        super("");
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
