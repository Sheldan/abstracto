package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.models.exception.TemplateNotFoundExceptionModel;
import dev.sheldan.abstracto.core.templating.Templatable;

public class TemplateNotFoundException extends AbstractoRunTimeException implements Templatable {

    private final TemplateNotFoundExceptionModel model;

    public TemplateNotFoundException(String templateKey) {
        super("Template not found.");
        this.model = TemplateNotFoundExceptionModel.builder().templateKey(templateKey).build();
    }

    @Override
    public String getTemplateName() {
        return "template_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return this.model;
    }
}
