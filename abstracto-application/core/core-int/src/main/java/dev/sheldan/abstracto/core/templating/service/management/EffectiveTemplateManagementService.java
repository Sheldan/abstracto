package dev.sheldan.abstracto.core.templating.service.management;

import dev.sheldan.abstracto.core.templating.model.EffectiveTemplate;
import dev.sheldan.abstracto.core.templating.model.database.Template;

import java.util.Optional;

/**
 * Provides methods to access the stored templates.
 */
public interface EffectiveTemplateManagementService {
    /**
     * Retrieves the template identified by the key.
     * @param key They template key to search for
     * @return An {@link Optional} containing the {@link Template} if it exists, and null otherwise
     */
    Optional<EffectiveTemplate> getTemplateByKeyAndServer(String key, Long server);
    Optional<EffectiveTemplate> getTemplateByKey(String key);

}
