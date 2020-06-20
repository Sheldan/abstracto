package dev.sheldan.abstracto.moderation.exception;


import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.templating.Templatable;

public class MuteException extends AbstractoRunTimeException implements Templatable {
    public MuteException(String message) {
        super(message);
    }

    @Override
    public String getTemplateName() {
        return "unMute_has_no_active_mute";
    }

    @Override
    public Object getTemplateModel() {
        return null;
    }
}

