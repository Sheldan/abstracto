package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.templating.Templatable;

import java.util.HashMap;

public class RoleNotFoundInDBException extends AbstractoRunTimeException implements Templatable {

    private final Long roleId;

    public RoleNotFoundInDBException(Long roleId) {
        super("");
        this.roleId = roleId;
    }

    @Override
    public String getTemplateName() {
        return "role_not_found_in_db_exception";
    }

    @Override
    public Object getTemplateModel() {
        HashMap<String, Long> param = new HashMap<>();
        param.put("roleId", this.roleId);
        return param;
    }
}
