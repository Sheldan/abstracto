package dev.sheldan.abstracto.utility.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.templating.Templatable;

public class RepostCheckChannelGroupNotFoundException extends AbstractoRunTimeException implements Templatable {

    public RepostCheckChannelGroupNotFoundException(Long channelGroupId) {
        super(String.format("Repost check channel with id %s does not exist", channelGroupId));
    }

    @Override
    public String getTemplateName() {
        return "repost_check_check_channel_not_found";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
