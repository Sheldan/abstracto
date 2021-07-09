package dev.sheldan.abstracto.assignableroles.service.management;

import dev.sheldan.abstracto.assignableroles.model.condition.AssignableRoleConditionType;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRole;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRoleCondition;

import java.util.Optional;

public interface AssignableRoleConditionManagementService {
    AssignableRoleCondition createAssignableRoleCondition(AssignableRole assignableRole, AssignableRoleConditionType type, String value);
    void deleteAssignableRoleCondition(AssignableRoleCondition condition);
    Optional<AssignableRoleCondition> findAssignableRoleCondition(AssignableRole role, AssignableRoleConditionType type);
}
