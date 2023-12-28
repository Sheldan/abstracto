
package dev.sheldan.abstracto.modmail.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

public class ModMailThreadClosedException extends AbstractoRunTimeException implements Templatable {

    public ModMailThreadClosedException() {
        super("Mod mail thread closed");
    }

    @Override
    public String getTemplateName() {
        return "modmail_thread_closed_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
