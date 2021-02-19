package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.models.exception.EmoteNotFoundExceptionModel;
import dev.sheldan.abstracto.core.templating.Templatable;

import java.util.List;

public class EmoteNotFoundException extends AbstractoRunTimeException implements Templatable {

    private final EmoteNotFoundExceptionModel model;

    public EmoteNotFoundException(String key, List<String> availableEmotes) {
        super("Emote not found");
        this.model = EmoteNotFoundExceptionModel.builder().emoteKey(key).available(availableEmotes).build();
    }

    @Override
    public String getTemplateName() {
        return "emote_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
