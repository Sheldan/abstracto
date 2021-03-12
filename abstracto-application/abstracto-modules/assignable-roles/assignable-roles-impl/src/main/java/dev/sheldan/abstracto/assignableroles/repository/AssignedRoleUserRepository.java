package dev.sheldan.abstracto.assignableroles.repository;

import dev.sheldan.abstracto.assignableroles.model.database.AssignedRoleUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository to manage the access to the table managed by {@link AssignedRoleUser assignedRoleUser}
 */
@Repository
public interface AssignedRoleUserRepository extends JpaRepository<AssignedRoleUser, Long> {
}
