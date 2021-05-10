package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.RoleImmunityId;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.EffectType;
import dev.sheldan.abstracto.core.models.database.RoleImmunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleImmunityRepository extends JpaRepository<RoleImmunity, RoleImmunityId> {
    List<RoleImmunity> findByServerAndEffect(AServer server, EffectType effectType);
}
