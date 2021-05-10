package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.EffectType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EffectTypeRepository extends JpaRepository<EffectType, Long> {
    Optional<EffectType> findByEffectTypeKey(String effectTypeKey);
}
