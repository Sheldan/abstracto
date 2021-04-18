package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.templating.Templatable;

public class ProfanityRegexNotFoundException extends AbstractoRunTimeException implements Templatable {

    public ProfanityRegexNotFoundException() {
        super("Profanity regex not found.");
    }

    @Override
    public String getTemplateName() {
        return "profanity_regex_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
