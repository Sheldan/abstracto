package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.templating.Templatable;

import java.util.HashMap;

public class GuildNotFoundException extends AbstractoRunTimeException implements Templatable {
    private final Long guildId;

    public GuildNotFoundException(String message, Long guildId) {
        super(message);
        this.guildId = guildId;
    }

    public GuildNotFoundException(Long guildId) {
        super("");
        this.guildId = guildId;
    }

    @Override
    public String getTemplateName() {
        return "guild_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        HashMap<String, Long> param = new HashMap<>();
        param.put("guildId", this.guildId);
        return param;
    }
}
