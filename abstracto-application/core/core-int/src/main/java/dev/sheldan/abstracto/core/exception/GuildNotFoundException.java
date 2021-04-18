package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.models.exception.GuildNotFoundExceptionModel;
import dev.sheldan.abstracto.core.templating.Templatable;

public class GuildNotFoundException extends AbstractoRunTimeException implements Templatable {
    private final GuildNotFoundExceptionModel model;

    public GuildNotFoundException(Long guildId) {
        super("Guild not found");
        this.model = GuildNotFoundExceptionModel
                .builder()
                .guildId(guildId)
                .build();
    }

    @Override
    public String getTemplateName() {
        return "guild_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
