package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.templating.Templatable;

import java.util.HashMap;

public class RoleNotFoundInDBException extends AbstractoRunTimeException implements Templatable {

    private final Long roleId;
    private final Long serverId;

    public RoleNotFoundInDBException(Long roleId, Long serverId) {
        super("");
        this.roleId = roleId;
        this.serverId = serverId;
    }

    @Override
    public String getTemplateName() {
        return "role_not_found_in_db_exception";
    }

    @Override
    public Object getTemplateModel() {
        HashMap<String, Long> param = new HashMap<>();
        param.put("roleId", this.roleId);
        param.put("serverId", this.serverId);
        return param;
    }
}
