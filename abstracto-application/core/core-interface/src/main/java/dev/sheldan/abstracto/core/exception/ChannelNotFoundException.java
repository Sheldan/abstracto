package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.templating.Templatable;

import java.util.HashMap;

public class ChannelNotFoundException extends AbstractoRunTimeException implements Templatable {

    private Long channelId;
    private Long guildId;

    public ChannelNotFoundException(String message) {
        super(message);
    }

    public ChannelNotFoundException(Long channelId, Long guildId) {
        super("");
        this.channelId = channelId;
        this.guildId = guildId;
    }

    @Override
    public String getTemplateName() {
        return "channel_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        HashMap<String, Long> param = new HashMap<>();
        param.put("channelId", this.channelId);
        param.put("guildID", this.guildId);
        return param;
    }
}
