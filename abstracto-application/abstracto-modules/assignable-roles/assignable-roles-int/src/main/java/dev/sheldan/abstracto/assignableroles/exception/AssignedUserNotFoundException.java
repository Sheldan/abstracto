package dev.sheldan.abstracto.assignableroles.exception;

import dev.sheldan.abstracto.assignableroles.model.exception.AssignedUserNotFoundExceptionModel;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.templating.Templatable;

/**
 * Exception which is thrown in case an {@link dev.sheldan.abstracto.assignableroles.model.database.AssignedRoleUser user}
 * was not found.
 */
public class AssignedUserNotFoundException extends AbstractoRunTimeException implements Templatable {

    private final AssignedUserNotFoundExceptionModel model;

    public AssignedUserNotFoundException(AUserInAServer userInAServer) {
        super("Assigned user was not found");
        this.model = AssignedUserNotFoundExceptionModel
                .builder()
                .userId(userInAServer.getUserReference().getId())
                .build();
    }

    @Override
    public String getTemplateName() {
        return "assignable_role_place_assigned_user_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
