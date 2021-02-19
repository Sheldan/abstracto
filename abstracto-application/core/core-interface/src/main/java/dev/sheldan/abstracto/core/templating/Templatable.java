package dev.sheldan.abstracto.core.templating;

/**
 * An interface to be used on objects, which should be able to be processable by the template engine.
 * This contains a template key and the model which is used when rendering this template.
 */
public interface Templatable {
    /**
     * The template key to be used to render this object.
     * @return The template key as string
     */
    String getTemplateName();

    /**
     * The model to be used to render this template.
     * @return The model containing the attributes to be used for rendering.
     */
    Object getTemplateModel();
}
