package dev.sheldan.abstracto.moderation.exception;


import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.templating.Templatable;

public class MuteRoleNotSetupException extends AbstractoRunTimeException implements Templatable {
    public MuteRoleNotSetupException() {
        super("Mute role for server has not been setup");
    }

    @Override
    public String getTemplateName() {
        return "mute_role_has_not_been_setup_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}

