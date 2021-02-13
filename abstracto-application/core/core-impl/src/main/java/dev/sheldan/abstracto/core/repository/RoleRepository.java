package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.ARole;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<ARole, Long> {
    @NotNull
    @Override
    Optional<ARole> findById(@NonNull Long aLong);
}
