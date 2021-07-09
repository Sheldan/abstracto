package dev.sheldan.abstracto.assignableroles.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

public class AssignableRolePlaceNotFoundException extends AbstractoRunTimeException implements Templatable {

    public AssignableRolePlaceNotFoundException() {
        super("Assignable role place not found");
    }

    @Override
    public String getTemplateName() {
        return "assignable_role_place_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
