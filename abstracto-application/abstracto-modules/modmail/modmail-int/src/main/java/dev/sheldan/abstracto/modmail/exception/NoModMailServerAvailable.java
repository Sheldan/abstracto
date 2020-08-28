package dev.sheldan.abstracto.modmail.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.templating.Templatable;

public class NoModMailServerAvailable extends AbstractoRunTimeException implements Templatable {

    public NoModMailServerAvailable() {
        super("No modmail server available");
    }

    @Override
    public String getTemplateName() {
        return "modmail_no_server_available_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
