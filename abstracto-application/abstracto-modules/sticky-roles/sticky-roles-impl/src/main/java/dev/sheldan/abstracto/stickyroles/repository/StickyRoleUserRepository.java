package dev.sheldan.abstracto.stickyroles.repository;

import dev.sheldan.abstracto.stickyroles.model.database.StickyRoleUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StickyRoleUserRepository extends JpaRepository<StickyRoleUser, Long> {
}
