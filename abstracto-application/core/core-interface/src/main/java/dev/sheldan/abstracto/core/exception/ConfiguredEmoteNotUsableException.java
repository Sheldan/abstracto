package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.exception.EmoteConfiguredButNotUsableExceptionModel;
import dev.sheldan.abstracto.templating.Templatable;

public class ConfiguredEmoteNotUsableException extends AbstractoRunTimeException implements Templatable {

    private final EmoteConfiguredButNotUsableExceptionModel model;

    public ConfiguredEmoteNotUsableException(AEmote emote) {
        super("Emote was configured in database, but is not usable by the bot anymore.");
        this.model = EmoteConfiguredButNotUsableExceptionModel.builder().emote(emote).build();
    }

    @Override
    public String getTemplateName() {
        return "emote_defined_but_not_usable_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
