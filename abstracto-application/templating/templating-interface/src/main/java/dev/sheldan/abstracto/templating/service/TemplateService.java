package dev.sheldan.abstracto.templating.service;

import dev.sheldan.abstracto.templating.Templatable;
import dev.sheldan.abstracto.templating.model.MessageToSend;

import java.util.HashMap;

/**
 * Provides methods to render templates with the appropriate model.
 */
public interface TemplateService {
    /**
     * The template containing a embed definition which should be rendered. The key must refer to an existing template and the supplied model will be used when rendering.
     * This creates a {@link MessageToSend} object containing the rendered template and might result in multiple embeds.
     * @param key The key of the embed template to be used for rendering.
     * @param model The model providing the properties to be used for rendering
     * @return A fully rendered message containing the content of the template and might contain multiple embeds.
     */
    MessageToSend renderEmbedTemplate(String key, Object model);

    /**
     * Renders the template identified by the key with the given {@link HashMap} used as model and returns the value as a string
     * @param key The key of the template to be rendered.
     * @param parameters The {@link HashMap} to be used as the parameters for the template
     * @return The template rendered as string.
     */
    String renderTemplateWithMap(String key, HashMap<String, Object> parameters);

    /**
     * Renders the template identified by the key with the given model and returns the value as a string
     * @param key The key of the template to be rendered
     * @param model The object containing the model to be used in the template
     * @return The template rendered as string.
     */
    String renderTemplate(String key, Object model);
    String renderSimpleTemplate(String key);

    String renderTemplatable(Templatable templatable);
}
