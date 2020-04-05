package dev.sheldan.abstracto.core.models.command;

import dev.sheldan.abstracto.core.models.database.AChannel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.TextChannel;

@Getter
@Setter
@Builder
public class ChannelGroupChannelModel {
    private AChannel channel;
    private TextChannel discordChannel;
}
