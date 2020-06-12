package dev.sheldan.abstracto.modmail.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.templating.Templatable;

/**
 * This exception is thrown then you try to subscribe to a mod mail thread, to which you are already subscribed to
 */
public class AlreadySubscribedException extends AbstractoRunTimeException implements Templatable {
    public AlreadySubscribedException() {
        super("");
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
