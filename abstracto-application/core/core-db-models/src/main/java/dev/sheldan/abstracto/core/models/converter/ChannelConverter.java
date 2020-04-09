package dev.sheldan.abstracto.core.models.converter;

import dev.sheldan.abstracto.core.models.AChannel;
import dev.sheldan.abstracto.core.models.AServer;
import dev.sheldan.abstracto.core.models.dto.ChannelDto;
import dev.sheldan.abstracto.core.models.dto.ServerDto;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChannelConverter {

    @Autowired
    private ServerConverter serverConverter;

    public ChannelDto fromChannel(AChannel channel) {
        ServerDto server = ServerDto.builder().id(channel.getServer().getId()).build();
        return ChannelDto
                .builder()
                .id(channel.getId())
                .deleted(channel.getDeleted())
                .server(server)
                .build();
    }

    public ChannelDto fromTextChannel(TextChannel channel) {

        return ChannelDto
                .builder()
                .id(channel.getIdLong())
                .deleted(false)
                .server(ServerDto.builder().id(channel.getGuild().getIdLong()).build())
                .build();
    }

    public AChannel fromDto(ChannelDto channelDto) {
        AServer server = AServer.builder().id(channelDto.getServer().getId()).build();
        return AChannel
                .builder()
                .deleted(channelDto.getDeleted())
                .server(server)
                .id(channelDto.getId())
                .build();
    }
}
