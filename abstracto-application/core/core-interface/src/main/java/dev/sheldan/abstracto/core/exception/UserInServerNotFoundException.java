package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.templating.Templatable;

import java.util.HashMap;

public class UserInServerNotFoundException extends AbstractoRunTimeException implements Templatable {

    private Long userInServerId;

    public UserInServerNotFoundException(Long userInServerId) {
        super("");
        this.userInServerId = userInServerId;
    }

    @Override
    public String getTemplateName() {
        return "core_user_in_server_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        HashMap<String, Long> param = new HashMap<>();
        param.put("userInServerId", this.userInServerId);
        return param;
    }
}
