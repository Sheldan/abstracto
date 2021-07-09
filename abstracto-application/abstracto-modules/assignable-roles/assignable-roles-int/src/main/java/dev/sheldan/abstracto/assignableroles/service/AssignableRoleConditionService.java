package dev.sheldan.abstracto.assignableroles.service;

import dev.sheldan.abstracto.assignableroles.model.condition.AssignableRoleConditionResult;
import dev.sheldan.abstracto.assignableroles.model.condition.AssignableRoleConditionType;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRoleCondition;
import dev.sheldan.abstracto.assignableroles.model.template.condition.AssignableRoleConditionDisplay;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;

public interface AssignableRoleConditionService {
    AssignableRoleConditionResult evaluateConditions(List<AssignableRoleCondition> conditions, Member member, Role role);
    AssignableRoleConditionResult evaluateConditions(List<AssignableRoleCondition> conditions, AUserInAServer aUserInAServer, Role role);
    AssignableRoleCondition createAssignableRoleCondition(String placeName, Role role, AssignableRoleConditionType type, String value);
    void deleteAssignableRoleCondition(String placeName, Role role, AssignableRoleConditionType type);
    List<AssignableRoleConditionDisplay> getConditionDisplays(List<AssignableRoleCondition> conditions);
}
