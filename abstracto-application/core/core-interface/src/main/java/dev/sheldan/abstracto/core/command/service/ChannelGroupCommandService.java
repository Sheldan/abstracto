package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.models.database.ACommand;
import dev.sheldan.abstracto.core.models.database.AChannel;

public interface ChannelGroupCommandService {
    Boolean isCommandEnabled(ACommand command, AChannel channel);
}
