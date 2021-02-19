package dev.sheldan.abstracto.core.templating.repository;

import dev.sheldan.abstracto.core.templating.model.database.CustomTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


/**
 * Repository used to load the templates from the database.
 */
@Repository
public interface CustomTemplateRepository extends JpaRepository<CustomTemplate, String> {
    Optional<CustomTemplate> findByKeyAndServerId(String key, Long serverId);
}
