package dev.sheldan.abstracto.modmail.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.templating.Templatable;

public class NotInModMailThreadException extends AbstractoRunTimeException implements Templatable {

    @Override
    public String getTemplateName() {
        return "modmail_not_in_modmail_thread_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
