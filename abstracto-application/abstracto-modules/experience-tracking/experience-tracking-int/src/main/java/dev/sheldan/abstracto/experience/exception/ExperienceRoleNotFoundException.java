package dev.sheldan.abstracto.experience.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;
import org.springframework.stereotype.Component;

@Component
public class ExperienceRoleNotFoundException extends AbstractoRunTimeException implements Templatable {

    public ExperienceRoleNotFoundException() {
        super("Experience role was not found for role.");
    }

    @Override
    public String getTemplateName() {
        return "experience_role_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
