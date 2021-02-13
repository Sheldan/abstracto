package dev.sheldan.abstracto.assignableroles.repository;

import dev.sheldan.abstracto.assignableroles.models.database.AssignedRoleUser;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssignedRoleUserRepository extends JpaRepository<AssignedRoleUser, Long> {
    @NotNull
    @Override
    Optional<AssignedRoleUser> findById(@NonNull Long aLong);

    @Override
    boolean existsById(@NonNull Long aLong);
}
