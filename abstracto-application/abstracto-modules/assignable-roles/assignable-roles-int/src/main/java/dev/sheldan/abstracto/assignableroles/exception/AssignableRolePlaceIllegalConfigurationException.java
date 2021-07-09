package dev.sheldan.abstracto.assignableroles.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;

public class AssignableRolePlaceIllegalConfigurationException extends AbstractoTemplatableException {

    public AssignableRolePlaceIllegalConfigurationException() {
        super("An illegal configuration key has been passed to configure the assignable role place config. Doing nothing.");
    }

    @Override
    public String getTemplateName() {
        return "assignable_role_place_illegal_configuration_key_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
