package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.models.exception.EmoteNotUsable;
import dev.sheldan.abstracto.templating.Templatable;
import net.dv8tion.jda.api.entities.Emote;

public class EmoteNotUsableException extends AbstractoRunTimeException implements Templatable {

    private EmoteNotUsable model;

    public EmoteNotUsableException(Emote emote) {
        super("");
        this.model = EmoteNotUsable.builder().emote(emote).build();
    }

    @Override
    public String getTemplateName() {
        return "emote_not_usable";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
