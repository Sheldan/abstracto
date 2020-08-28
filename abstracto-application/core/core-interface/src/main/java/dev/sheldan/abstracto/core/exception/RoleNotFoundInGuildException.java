package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.models.exception.RoleNotFoundInGuildExceptionModel;
import dev.sheldan.abstracto.templating.Templatable;

public class RoleNotFoundInGuildException extends AbstractoRunTimeException implements Templatable {

    private final RoleNotFoundInGuildExceptionModel model;

    public RoleNotFoundInGuildException(Long roleId, Long serverId) {
        super("Role not found in guild");
        this.model = RoleNotFoundInGuildExceptionModel.builder().roleId(roleId).serverId(serverId).build();
    }

    @Override
    public String getTemplateName() {
        return "role_not_found_in_guild_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
