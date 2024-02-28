package dev.sheldan.abstracto.experience.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

public class LevelActionNotFoundException extends AbstractoRunTimeException implements Templatable {

    public LevelActionNotFoundException() {
        super("Level action not found.");
    }

    @Override
    public String getTemplateName() {
        return "level_action_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
