package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.models.exception.UserInServerNotFoundExceptionModel;
import dev.sheldan.abstracto.core.templating.Templatable;

public class UserInServerNotFoundException extends AbstractoRunTimeException implements Templatable {

    private final UserInServerNotFoundExceptionModel model;

    public UserInServerNotFoundException(Long userInServerId) {
        super("User in server not found");
        this.model = UserInServerNotFoundExceptionModel.builder().userInServerId(userInServerId).build();
    }

    @Override
    public String getTemplateName() {
        return "user_in_server_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
