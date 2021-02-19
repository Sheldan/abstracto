package dev.sheldan.abstracto.moderation.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

public class NoMuteFoundException extends AbstractoRunTimeException implements Templatable {

    public NoMuteFoundException() {
        super("No mute found for user.");
    }

    @Override
    public String getTemplateName() {
        return "unMute_has_no_active_mute_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
