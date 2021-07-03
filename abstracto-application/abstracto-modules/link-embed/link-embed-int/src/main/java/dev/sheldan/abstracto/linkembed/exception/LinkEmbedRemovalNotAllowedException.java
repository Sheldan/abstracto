package dev.sheldan.abstracto.linkembed.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;

public class LinkEmbedRemovalNotAllowedException extends AbstractoTemplatableException {

    public LinkEmbedRemovalNotAllowedException() {
        super("User is not allowed remove the embedded link.");
    }

    @Override
    public String getTemplateName() {
        return "link_embed_removal_not_allowed_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
