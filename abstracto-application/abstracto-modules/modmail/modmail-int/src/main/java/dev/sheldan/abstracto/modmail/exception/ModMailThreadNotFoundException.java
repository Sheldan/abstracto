package dev.sheldan.abstracto.modmail.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.modmail.models.template.exception.ModMailThreadNotFoundExceptionModel;
import dev.sheldan.abstracto.core.templating.Templatable;

/**
 * This exception is raised, when for some reason the mod mail thread is not found in the database anymore, but the context which is executed stems from a mod mail thread.
 * For example if it is attempted to log a thread, without the thread existing in the database.
 */
public class ModMailThreadNotFoundException extends AbstractoRunTimeException implements Templatable {

    private final ModMailThreadNotFoundExceptionModel model;

    public ModMailThreadNotFoundException(Long modMailThreadId) {
        super("Mod mail thread not found");
        this.model = ModMailThreadNotFoundExceptionModel.builder().modMailThreadId(modMailThreadId).build();
    }

    @Override
    public String getTemplateName() {
        return "modmail_cannot_find_modmail_thread_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
