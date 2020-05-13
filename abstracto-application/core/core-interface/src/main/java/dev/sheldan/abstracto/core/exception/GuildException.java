package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.templating.Templatable;

public class GuildException extends AbstractoRunTimeException implements Templatable {
    public GuildException(String message) {
        super(message);
    }

    @Override
    public String getTemplateName() {
        return null;
    }

    @Override
    public Object getTemplateModel() {
        return null;
    }
}
