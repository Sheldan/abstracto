package dev.sheldan.abstracto.webservices.urban.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

public class NoUrbanDefinitionFoundException extends AbstractoRunTimeException implements Templatable {
    public NoUrbanDefinitionFoundException() {
        super("No urban definition found.");
    }

    @Override
    public String getTemplateName() {
        return "no_urban_definition_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
