package dev.sheldan.abstracto.moderation.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.templating.Templatable;

public class AllowedInviteLinkNotFound extends AbstractoRunTimeException implements Templatable {

    public AllowedInviteLinkNotFound(String message) {
        super(message);
    }

    @Override
    public String getTemplateName() {
        return "allowed_invite_link_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
