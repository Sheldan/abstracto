package dev.sheldan.abstracto.experience.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

public class NoExperienceTrackedException extends AbstractoRunTimeException implements Templatable {
    @Override
    public String getTemplateName() {
        return "no_experience_tracked_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
