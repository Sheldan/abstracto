package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.templating.Templatable;

public class UnknownMentionTypeException extends AbstractoRunTimeException implements Templatable {

    public UnknownMentionTypeException() {
        super("Unknown mention type");
    }

    @Override
    public String getTemplateName() {
        return "unknown_mention_type_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
