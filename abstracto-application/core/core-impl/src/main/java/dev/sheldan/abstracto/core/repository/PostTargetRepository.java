package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.AServer;
import dev.sheldan.abstracto.core.models.PostTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostTargetRepository extends JpaRepository<PostTarget, Long> {

    PostTarget findPostTargetByNameAndServerReference(String name, AServer server);

}
