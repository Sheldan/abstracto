package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.templating.Templatable;

public class EffectTypeNotFoundException extends AbstractoRunTimeException implements Templatable {

    public EffectTypeNotFoundException() {
        super("Effect type not found.");
    }

    @Override
    public String getTemplateName() {
        return "effect_type_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
