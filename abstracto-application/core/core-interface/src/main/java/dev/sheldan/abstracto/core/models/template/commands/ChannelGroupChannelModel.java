package dev.sheldan.abstracto.core.models.template.commands;

import dev.sheldan.abstracto.core.models.dto.ChannelDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.TextChannel;

@Getter
@Setter
@Builder
public class ChannelGroupChannelModel {
    private ChannelDto channel;
    private TextChannel discordChannel;
}
