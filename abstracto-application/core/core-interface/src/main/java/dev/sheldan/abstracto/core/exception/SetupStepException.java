package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.templating.Templatable;

public class SetupStepException extends AbstractoRunTimeException implements Templatable {
    public SetupStepException(String message) {
        super(message);
    }

    @Override
    public String getTemplateName() {
        return "setup_configuration_timeout";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
