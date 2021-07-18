package dev.sheldan.abstracto.assignableroles.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;

public class BoosterAssignableRolePlaceMemberNotBoostingException extends AbstractoTemplatableException {
    @Override
    public String getTemplateName() {
        return "assignable_role_booster_place_member_not_boosting_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
