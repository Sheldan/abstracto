package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.PostTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostTargetRepository extends JpaRepository<PostTarget, Long> {

    PostTarget findPostTargetByNameAndServerReference(String name, AServer server);

    boolean existsByNameAndServerReference(String name, AServer server);

    List<PostTarget> findByServerReference(AServer server);

}
