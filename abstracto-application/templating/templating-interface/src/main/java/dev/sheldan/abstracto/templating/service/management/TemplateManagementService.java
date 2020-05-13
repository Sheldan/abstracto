package dev.sheldan.abstracto.templating.service.management;

import dev.sheldan.abstracto.templating.model.database.Template;

import java.util.Optional;

/**
 * Provides methods to access the stored templates.
 */
public interface TemplateManagementService {
    /**
     * Retrieves the template identified by the key.
     * @param key They template key to search for
     * @return An {@link Optional} containing the {@link Template} if it exists, and null otherwise
     */
    Optional<Template> getTemplateByKey(String key);

    /**
     * Checks whether or not the template exists in the database.
     * @param key They key of the template to search for
     * @return true, if the template exists and false otherwise
     */
    boolean templateExists(String key);

    /**
     * Creates a template identified by the key and with the provided content.
     * @param key They key of the template to create.
     * @param content The content the template should have
     * @return The created template in the database.
     */
    Template createTemplate(String key, String content);
}
