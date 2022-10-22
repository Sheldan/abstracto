package dev.sheldan.abstracto.webservices.threadreader.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;

public class NoTwitterLinkFoundException extends AbstractoTemplatableException {
    @Override
    public String getTemplateName() {
        return "no_twitter_link_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
