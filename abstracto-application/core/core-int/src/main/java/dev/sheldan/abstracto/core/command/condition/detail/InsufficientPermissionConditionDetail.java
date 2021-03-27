package dev.sheldan.abstracto.core.command.condition.detail;

import dev.sheldan.abstracto.core.command.condition.ConditionDetail;
import dev.sheldan.abstracto.core.command.model.condition.InsufficientPermissionCondtionDetailModel;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;

public class InsufficientPermissionConditionDetail implements ConditionDetail {

    private final InsufficientPermissionCondtionDetailModel model;

    public InsufficientPermissionConditionDetail(List<Role> allowedRoles) {
        this.model = InsufficientPermissionCondtionDetailModel.builder().allowedRoles(allowedRoles).build();
    }

    @Override
    public String getTemplateName() {
        return "insufficient_role_condition";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
