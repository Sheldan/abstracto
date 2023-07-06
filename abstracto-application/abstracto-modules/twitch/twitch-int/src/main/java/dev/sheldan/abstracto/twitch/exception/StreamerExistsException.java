package dev.sheldan.abstracto.twitch.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

public class StreamerExistsException extends AbstractoRunTimeException implements Templatable {
    public StreamerExistsException() {
        super("Streamer is already setup in server.");
    }

    @Override
    public String getTemplateName() {
        return "streamer_already_exists_in_server_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
