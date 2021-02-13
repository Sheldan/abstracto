package dev.sheldan.abstracto.assignableroles.repository;

import dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlace;
import dev.sheldan.abstracto.core.models.database.AServer;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignableRolePlaceRepository extends JpaRepository<AssignableRolePlace, Long> {

    boolean existsByServerAndKey(AServer server, String key);

    Optional<AssignableRolePlace> findByServerAndKey(AServer server, String key);

    List<AssignableRolePlace> findByServer(AServer server);

    @NotNull
    @Override
    Optional<AssignableRolePlace> findById(@NonNull Long aLong);
}
