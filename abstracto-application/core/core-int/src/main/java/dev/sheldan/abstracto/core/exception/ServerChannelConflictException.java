package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.models.exception.ServerChannelConflictExceptionModel;
import dev.sheldan.abstracto.core.templating.Templatable;

public class ServerChannelConflictException extends AbstractoRunTimeException implements Templatable {

    private final ServerChannelConflictExceptionModel model;

    public ServerChannelConflictException(Long serverId, Long channelId) {
        super("Given channel was not part of the assumed server.");
        this.model = ServerChannelConflictExceptionModel.builder().channelId(channelId).serverId(serverId).build();
    }

    @Override
    public String getTemplateName() {
        return "server_channel_conflict_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
