package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.PostTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostTargetRepository extends JpaRepository<PostTarget, Long> {

    Optional<PostTarget> findPostTargetByNameAndServerReference(String name, AServer server);

    boolean existsByNameAndServerReference(String name, AServer server);

    List<PostTarget> findByServerReference(AServer server);

}
