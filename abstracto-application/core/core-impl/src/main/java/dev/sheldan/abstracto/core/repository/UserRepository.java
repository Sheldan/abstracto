package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.AUser;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AUser, Long> {
    @NotNull
    @Override
    Optional<AUser> findById(@NonNull Long aLong);

    @Override
    boolean existsById(@NonNull Long aLong);
}
