package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.templating.Templatable;

public class ProfanityGroupExistsException extends AbstractoRunTimeException implements Templatable {

    public ProfanityGroupExistsException() {
        super("Profanity group already exists.");
    }

    @Override
    public String getTemplateName() {
        return "profanity_group_exists_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
