package dev.sheldan.abstracto.core.templating.repository;

import dev.sheldan.abstracto.core.templating.model.database.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * Repository used to load the templates from the database.
 */
@Repository
public interface TemplateRepository extends JpaRepository<Template, String> {
}
