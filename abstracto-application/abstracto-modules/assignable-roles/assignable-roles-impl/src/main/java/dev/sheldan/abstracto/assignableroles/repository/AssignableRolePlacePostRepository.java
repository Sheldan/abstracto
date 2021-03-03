package dev.sheldan.abstracto.assignableroles.repository;

import dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlacePost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository to manage the access to the table managed by {@link AssignableRolePlacePost posts}
 */
@Repository
public interface AssignableRolePlacePostRepository extends JpaRepository<AssignableRolePlacePost, Long> {
}
