package dev.sheldan.abstracto.core.models.template.commands;


import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import dev.sheldan.abstracto.core.models.template.display.ChannelDisplay;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class EchoRedirectResponseModel extends UserInitiatedServerContext {
    private ChannelDisplay channel;
}
