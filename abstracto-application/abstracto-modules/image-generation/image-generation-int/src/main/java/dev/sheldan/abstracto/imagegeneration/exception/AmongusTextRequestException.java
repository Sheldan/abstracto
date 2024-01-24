package dev.sheldan.abstracto.imagegeneration.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;
import dev.sheldan.abstracto.imagegeneration.model.exception.AmongusTextRequestExceptionModel;

public class AmongusTextRequestException extends AbstractoTemplatableException {
    private final AmongusTextRequestExceptionModel model;

    public AmongusTextRequestException(String inputText, String errorMessage) {
        this.model = AmongusTextRequestExceptionModel
                .builder()
                .inputText(inputText)
                .errorMessage(errorMessage)
                .build();
    }

    @Override
    public String getTemplateName() {
        return "amongusText_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
