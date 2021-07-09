package dev.sheldan.abstracto.assignableroles.condition;

import dev.sheldan.abstracto.assignableroles.model.condition.AssignableRoleConditionType;
import dev.sheldan.abstracto.assignableroles.model.condition.AssignableRolePlaceConditionModel;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRoleCondition;
import dev.sheldan.abstracto.assignableroles.model.template.condition.AssignableRoleConditionDisplay;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import net.dv8tion.jda.api.entities.Role;

public interface AssignableRoleConditionEvaluator {
    boolean handlesCondition(AssignableRoleConditionType type);
    boolean fulfillsCondition(AssignableRoleCondition conditionDefinition, AUserInAServer aUserInAServer);
    boolean usableValue(String value);
    AssignableRolePlaceConditionModel createNotificationModel(AssignableRoleCondition conditionDefinition, Role role);
    AssignableRoleConditionDisplay getConditionDisplay(AssignableRoleCondition conditionDefinition);
}
