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
     * Renders the given template directly into a {@link MessageToSend} but the template only represents a text message.
     * @param key The key of the string template to be rendered.
     * @param model The model used to render the template
     * @return A {@link MessageToSend} instance only containing the string property.
     */
    MessageToSend renderTemplateToMessageToSend(String key, Object model);

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

    /**
     * Renders the template without considering any model to be used. If a property is used in the given template, this will throw an exception.
     * This is just a quick way to render templates, which do not *need* a model.
     * @param key The key of the template to be rendered
     * @return The template rendered as string
     */
    String renderSimpleTemplate(String key);

    /**
     * Renders the given {@link Templatable} object, which means it retrieves the template key and renders the given template key with the given model.
     * @param templatable The {@link Templatable} object to be rendered
     * @return The template rendered as string
     */
    String renderTemplatable(Templatable templatable);

    void clearCache();
}
