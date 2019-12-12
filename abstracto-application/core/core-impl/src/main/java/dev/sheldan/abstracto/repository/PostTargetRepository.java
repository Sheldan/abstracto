package dev.sheldan.abstracto.repository;

import dev.sheldan.abstracto.core.models.PostTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostTargetRepository extends JpaRepository<PostTarget, Long> {

    PostTarget findPostTargetByName(String name);

}
