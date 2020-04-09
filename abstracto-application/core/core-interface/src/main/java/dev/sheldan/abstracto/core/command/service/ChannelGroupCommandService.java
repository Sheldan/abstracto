package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.models.dto.ChannelDto;
import dev.sheldan.abstracto.core.models.dto.CommandDto;

public interface ChannelGroupCommandService {
    Boolean isCommandEnabled(CommandDto command, ChannelDto channel);
}
