package dev.sheldan.abstracto.assignableroles.repository;

import dev.sheldan.abstracto.assignableroles.model.database.AssignableRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository to manage the access to the table managed by {@link AssignableRole assignableRole}
 */
@Repository
public interface AssignableRoleRepository extends JpaRepository<AssignableRole, Long> {
}
