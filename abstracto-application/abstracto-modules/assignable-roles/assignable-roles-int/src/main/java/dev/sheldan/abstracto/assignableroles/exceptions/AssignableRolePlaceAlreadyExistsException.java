package dev.sheldan.abstracto.assignableroles.exceptions;

import dev.sheldan.abstracto.assignableroles.models.exception.AssignableRolePlaceAlreadyExistsModel;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.templating.Templatable;

public class AssignableRolePlaceAlreadyExistsException extends AbstractoRunTimeException implements Templatable {

    private final AssignableRolePlaceAlreadyExistsModel model;

    public AssignableRolePlaceAlreadyExistsException(String name) {
        super("Assignable role place already exists");
        this.model = AssignableRolePlaceAlreadyExistsModel.builder().name(name).build();
    }

    @Override
    public String getTemplateName() {
        return "assignable_role_place_exists_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
