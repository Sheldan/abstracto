package dev.sheldan.abstracto.assignableroles.repository;

import dev.sheldan.abstracto.assignableroles.model.database.AssignableRole;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlaceType;
import dev.sheldan.abstracto.assignableroles.model.database.AssignedRoleUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository to manage the access to the table managed by {@link AssignableRole assignableRole}
 */
@Repository
public interface AssignableRoleRepository extends JpaRepository<AssignableRole, Long> {
    List<AssignableRole> findByAssignedUsersContainingAndAssignablePlace_Type(AssignedRoleUser roleUser, AssignableRolePlaceType type);
}
