package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.AServer;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServerRepository extends JpaRepository<AServer, Long> {

    @NotNull
    @Override
    Optional<AServer> findById(@NonNull Long aLong);

    @Override
    boolean existsById(@NonNull Long aLong);

    @NotNull
    @Override
    List<AServer> findAll();
}
