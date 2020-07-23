package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.templating.Templatable;

import java.util.HashMap;

public class ChannelNotFoundException extends AbstractoRunTimeException implements Templatable {

    private final Long channelId;

    public ChannelNotFoundException(Long channelId) {
        super("Channel not found in database");
        this.channelId = channelId;
    }

    @Override
    public String getTemplateName() {
        return "channel_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        HashMap<String, Long> param = new HashMap<>();
        param.put("channelId", this.channelId);
        return param;
    }
}
