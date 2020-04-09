package dev.sheldan.abstracto.core.models.converter;

import dev.sheldan.abstracto.core.models.AChannel;
import dev.sheldan.abstracto.core.models.AServer;
import dev.sheldan.abstracto.core.models.AUserInAServer;
import dev.sheldan.abstracto.core.models.dto.ChannelDto;
import dev.sheldan.abstracto.core.models.dto.ServerDto;
import dev.sheldan.abstracto.core.models.dto.UserInServerDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ServerConverter {

    @Autowired
    private ChannelConverter channelConverter;

    @Autowired
    private UserInServerConverter userInServerConverter;

    public ServerDto convertServer(AServer server) {
        List<ChannelDto> channels = new ArrayList<>();
        server.getChannels().forEach(channel -> {
            channels.add(channelConverter.fromChannel(channel));
        });

        List<UserInServerDto> users = new ArrayList<>();
        server.getUsers().forEach(aUserInAServer -> {
            users.add(userInServerConverter.fromAUserInAServer(aUserInAServer));
        });
        return ServerDto
                .builder()
                .id(server.getId())
                .channels(channels)
                .users(users)
                .name(server.getName())
                .build();
    }

    public AServer fromDto(ServerDto serverDto) {
        List<AChannel> channels = new ArrayList<>();
        serverDto.getChannels().forEach(channelDto -> {
            channels.add(channelConverter.fromDto(channelDto));
        });

        List<AUserInAServer> users = new ArrayList<>();
        serverDto.getUsers().forEach(userInServerDto -> {
            users.add(userInServerConverter.fromDto(userInServerDto));
        });
        return AServer
                .builder()
                .name(serverDto.getName())
                .id(serverDto.getId())
                .users(users)
                .channels(channels)
                .build();
    }
}
