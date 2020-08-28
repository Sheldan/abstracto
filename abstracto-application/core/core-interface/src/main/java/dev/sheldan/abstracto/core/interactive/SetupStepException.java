package dev.sheldan.abstracto.core.interactive;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.templating.Templatable;

public class SetupStepException extends AbstractoRunTimeException implements Templatable {

    private final SetupStepExceptionModel model;

    public SetupStepException(Throwable throwable) {
        super("Failed to execute setup step", throwable);
        SetupStepExceptionModel.SetupStepExceptionModelBuilder builder = SetupStepExceptionModel.builder();
        if(getCause() instanceof Templatable) {
            Templatable templatable = (Templatable) getCause();
            builder.templateKey(templatable.getTemplateName());
            builder.templateModel(templatable.getTemplateModel());
        }
        builder.message(getCause().getMessage());
        this.model = builder.build();
    }

    @Override
    public String getTemplateName() {
        return "setup_step_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
