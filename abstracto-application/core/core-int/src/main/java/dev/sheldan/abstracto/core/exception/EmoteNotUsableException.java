package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.models.exception.EmoteNotUsableExceptionModel;
import dev.sheldan.abstracto.core.templating.Templatable;
import net.dv8tion.jda.api.entities.Emote;

public class EmoteNotUsableException extends AbstractoRunTimeException implements Templatable {

    private final EmoteNotUsableExceptionModel model;

    public EmoteNotUsableException(Emote emote) {
        super(String.format("Emote %s not usable by bot.", emote.getId()));
        this.model = EmoteNotUsableExceptionModel
                .builder()
                .emote(emote)
                .build();
    }

    @Override
    public String getTemplateName() {
        return "emote_not_usable_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
