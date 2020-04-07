package dev.sheldan.abstracto.templating.loading;

import dev.sheldan.abstracto.templating.model.database.Template;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplateRepository extends JpaRepository<Template, String> {
}
