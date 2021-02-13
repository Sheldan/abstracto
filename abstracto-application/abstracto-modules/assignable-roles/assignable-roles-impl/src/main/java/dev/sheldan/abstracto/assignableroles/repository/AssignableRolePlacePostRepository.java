package dev.sheldan.abstracto.assignableroles.repository;

import dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlacePost;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssignableRolePlacePostRepository extends JpaRepository<AssignableRolePlacePost, Long> {
    @NotNull
    @Override
    Optional<AssignableRolePlacePost> findById(@NonNull Long aLong);
}
