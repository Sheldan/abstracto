package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.models.exception.EmoteNotInServerModel;
import dev.sheldan.abstracto.templating.Templatable;

public class ExceptionNotInServerException extends AbstractoRunTimeException implements Templatable {

    private EmoteNotInServerModel model;

    public ExceptionNotInServerException(Long emoteId) {
        super("Emote not available in server");
        this.model = EmoteNotInServerModel.builder().emoteId(emoteId).build();
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
