package dev.sheldan.abstracto.twitch.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

public class StreamerNotFoundInServerException extends AbstractoRunTimeException implements Templatable {
    public StreamerNotFoundInServerException() {
        super("Streamer was not set up in server.");
    }

    @Override
    public String getTemplateName() {
        return "streamer_not_exists_in_server_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
