package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.models.exception.EmoteNotInServerExceptionModel;
import dev.sheldan.abstracto.core.templating.Templatable;

public class EmoteNotInServerException extends AbstractoRunTimeException implements Templatable {

    private final EmoteNotInServerExceptionModel model;

    public EmoteNotInServerException(Long emoteId) {
        super("Emote not available in server");
        this.model = EmoteNotInServerExceptionModel
                .builder()
                .emoteId(emoteId)
                .build();
    }

    @Override
    public String getTemplateName() {
        return "emote_not_available_in_server_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
