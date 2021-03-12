package dev.sheldan.abstracto.assignableroles.exception;

import dev.sheldan.abstracto.assignableroles.model.exception.AssignableRolePlacePostNotFoundExceptionModel;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

/**
 * Exception which is thrown, in case the {@link dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlacePost post}
 * was not found via its message ID
 */
public class AssignableRolePlacePostNotFoundException extends AbstractoRunTimeException implements Templatable {

    private final AssignableRolePlacePostNotFoundExceptionModel model;

    public AssignableRolePlacePostNotFoundException(Long messageId) {
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
