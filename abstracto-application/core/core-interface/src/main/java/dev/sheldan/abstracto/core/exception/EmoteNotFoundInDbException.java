package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.models.exception.EmoteNotFoundInDbExceptionModel;
import dev.sheldan.abstracto.templating.Templatable;

public class EmoteNotFoundInDbException extends AbstractoRunTimeException implements Templatable {

    private final EmoteNotFoundInDbExceptionModel model;

    public EmoteNotFoundInDbException(Integer emoteId) {
        super("Emote not found in database");
        this.model = EmoteNotFoundInDbExceptionModel.builder().emoteId(emoteId).build();
    }

    @Override
    public String getTemplateName() {
        return "emote_not_found_in_db_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
