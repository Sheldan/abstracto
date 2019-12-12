package dev.sheldan.abstracto.repository;

import dev.sheldan.abstracto.core.models.ARole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<ARole, Long> {
}
