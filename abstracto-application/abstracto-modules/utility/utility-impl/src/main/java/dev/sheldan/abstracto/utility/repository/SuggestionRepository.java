package dev.sheldan.abstracto.utility.repository;

import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.utility.models.database.Suggestion;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SuggestionRepository extends JpaRepository<Suggestion, ServerSpecificId> {
    @NotNull
    @Override
    Optional<Suggestion> findById(@NonNull ServerSpecificId aLong);
}
