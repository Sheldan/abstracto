package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.DefaultPostTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DefaultPostTargetRepository extends JpaRepository<DefaultPostTarget, Long> {
}
