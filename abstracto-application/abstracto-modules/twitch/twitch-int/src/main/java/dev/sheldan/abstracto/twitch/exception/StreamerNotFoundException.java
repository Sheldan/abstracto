package dev.sheldan.abstracto.twitch.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

public class StreamerNotFoundException extends AbstractoRunTimeException implements Templatable {
    public StreamerNotFoundException() {
        super("Streamer was not found on Twitch..");
    }

    @Override
    public String getTemplateName() {
        return "streamer_not_exists_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
