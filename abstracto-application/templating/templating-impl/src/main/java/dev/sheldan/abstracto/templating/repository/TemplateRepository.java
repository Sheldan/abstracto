package dev.sheldan.abstracto.templating.repository;

import dev.sheldan.abstracto.templating.model.database.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository used to load the templates from the database.
 */
@Repository
public interface TemplateRepository extends JpaRepository<Template, String> {
}
