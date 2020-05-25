package dev.sheldan.abstracto.core.interactive;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.templating.Templatable;

public class NoChannelProvidedException extends AbstractoRunTimeException implements Templatable {
    public NoChannelProvidedException(String message) {
        super(message);
    }

    @Override
    public String getTemplateName() {
        return "setup_no_channel_provided_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
