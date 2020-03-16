package dev.sheldan.abstracto.templating.loading;

import dev.sheldan.abstracto.templating.TemplateDto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplateRepository extends JpaRepository<TemplateDto, String> {
}
