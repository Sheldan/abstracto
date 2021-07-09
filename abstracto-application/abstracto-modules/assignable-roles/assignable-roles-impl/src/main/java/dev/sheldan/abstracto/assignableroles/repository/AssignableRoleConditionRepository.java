package dev.sheldan.abstracto.assignableroles.repository;

import dev.sheldan.abstracto.assignableroles.model.condition.AssignableRoleConditionType;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRole;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRoleCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssignableRoleConditionRepository extends JpaRepository<AssignableRoleCondition, Long> {
    Optional<AssignableRoleCondition> findByAssignableRoleAndType(AssignableRole assignableRole, AssignableRoleConditionType type);
}
