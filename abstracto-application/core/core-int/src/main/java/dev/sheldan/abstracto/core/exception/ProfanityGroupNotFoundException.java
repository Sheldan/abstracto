package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.templating.Templatable;

public class ProfanityGroupNotFoundException extends AbstractoRunTimeException implements Templatable {

    public ProfanityGroupNotFoundException() {
        super("Profanity group not found.");
    }

    @Override
    public String getTemplateName() {
        return "profanity_group_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
