package dev.sheldan.abstracto.templating.repository;

import dev.sheldan.abstracto.templating.model.database.Template;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.util.Optional;

/**
 * Repository used to load the templates from the database.
 */
@Repository
public interface TemplateRepository extends JpaRepository<Template, String> {
    @NotNull
    @Override
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    Optional<Template> findById(@NonNull String aLong);

    @Override
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    boolean existsById(@NonNull String aLong);
}
