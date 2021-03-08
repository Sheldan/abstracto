package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.models.exception.CustomTemplateNotFoundExceptionModel;
import dev.sheldan.abstracto.core.templating.Templatable;
import net.dv8tion.jda.api.entities.Guild;

public class CustomTemplateNotFoundException extends AbstractoRunTimeException implements Templatable {

    private final CustomTemplateNotFoundExceptionModel model;

    public CustomTemplateNotFoundException(String templateKey, Guild guild) {
        super("Custom template not found.");
        this.model = CustomTemplateNotFoundExceptionModel
                .builder()
                .templateKey(templateKey)
                .guild(guild)
                .build();
    }

    @Override
    public String getTemplateName() {
        return "custom_template_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return this.model;
    }
}
