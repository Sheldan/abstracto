package dev.sheldan.abstracto.experience.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

public class LevelActionAlreadyExistsException extends AbstractoRunTimeException implements Templatable {

    public LevelActionAlreadyExistsException() {
        super("Level action already exists.");
    }

    @Override
    public String getTemplateName() {
        return "level_action_already_exists_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
