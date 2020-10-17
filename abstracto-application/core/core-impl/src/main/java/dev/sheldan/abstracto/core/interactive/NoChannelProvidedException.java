package dev.sheldan.abstracto.core.interactive;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.templating.Templatable;

public class NoChannelProvidedException extends AbstractoRunTimeException implements Templatable {
    public NoChannelProvidedException() {
        super("No channel was provided");
    }

    @Override
    public String getTemplateName() {
        return "feature_setup_no_channel_provided_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
