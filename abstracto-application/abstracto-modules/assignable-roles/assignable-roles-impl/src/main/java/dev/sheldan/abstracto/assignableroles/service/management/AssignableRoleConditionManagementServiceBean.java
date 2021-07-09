package dev.sheldan.abstracto.assignableroles.service.management;

import dev.sheldan.abstracto.assignableroles.model.condition.AssignableRoleConditionType;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRole;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRoleCondition;
import dev.sheldan.abstracto.assignableroles.repository.AssignableRoleConditionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AssignableRoleConditionManagementServiceBean implements AssignableRoleConditionManagementService {

    @Autowired
    private AssignableRoleConditionRepository repository;

    @Override
    public AssignableRoleCondition createAssignableRoleCondition(AssignableRole assignableRole, AssignableRoleConditionType type, String value) {
        AssignableRoleCondition condition = AssignableRoleCondition
                .builder()
                .assignableRole(assignableRole)
                .type(type)
                .conditionValue(value)
                .build();
        assignableRole.getConditions().add(condition);
        return repository.save(condition);
    }

    @Override
    public void deleteAssignableRoleCondition(AssignableRoleCondition condition) {
        repository.delete(condition);
    }

    @Override
    public Optional<AssignableRoleCondition> findAssignableRoleCondition(AssignableRole role, AssignableRoleConditionType type) {
        return repository.findByAssignableRoleAndType(role, type);
    }
}
