package dev.sheldan.abstracto.assignableroles.exceptions;

import dev.sheldan.abstracto.assignableroles.models.exception.AssignableRolePlacePostNotFoundExceptionModel;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

public class AssignablePlacePostNotFoundException extends AbstractoRunTimeException implements Templatable {

    private final AssignableRolePlacePostNotFoundExceptionModel model;

    public AssignablePlacePostNotFoundException(Long messageId) {
        super("Assignable place post not found.");
        this.model = AssignableRolePlacePostNotFoundExceptionModel.builder().messageId(messageId).build();
    }

    @Override
    public String getTemplateName() {
        return "assignable_role_place_post_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
