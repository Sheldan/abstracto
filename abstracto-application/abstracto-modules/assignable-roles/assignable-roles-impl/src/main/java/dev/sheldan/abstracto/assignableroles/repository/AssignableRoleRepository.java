package dev.sheldan.abstracto.assignableroles.repository;

import dev.sheldan.abstracto.assignableroles.models.database.AssignableRole;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssignableRoleRepository extends JpaRepository<AssignableRole, Long> {
    @NotNull
    @Override
    Optional<AssignableRole> findById(@NonNull Long aLong);
}
