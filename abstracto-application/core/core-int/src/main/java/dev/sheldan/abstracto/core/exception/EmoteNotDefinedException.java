package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.models.exception.EmoteNotDefinedExceptionModel;
import dev.sheldan.abstracto.core.templating.Templatable;

public class EmoteNotDefinedException extends AbstractoRunTimeException implements Templatable {

    private final EmoteNotDefinedExceptionModel model;

    public EmoteNotDefinedException(String key) {
        super(String.format("Emote %s not defined", key));
        this.model = EmoteNotDefinedExceptionModel
                .builder()
                .emoteKey(key)
                .build();
    }

    @Override
    public String getTemplateName() {
        return "emote_not_defined_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
