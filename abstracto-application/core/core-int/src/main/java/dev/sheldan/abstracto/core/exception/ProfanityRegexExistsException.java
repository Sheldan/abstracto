package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.templating.Templatable;

public class ProfanityRegexExistsException extends AbstractoRunTimeException implements Templatable {

    public ProfanityRegexExistsException() {
        super("Profanity regex already exists.");
    }

    @Override
    public String getTemplateName() {
        return "profanity_regex_exists_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
