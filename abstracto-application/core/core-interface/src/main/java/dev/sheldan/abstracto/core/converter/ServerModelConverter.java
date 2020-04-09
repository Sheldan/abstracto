package dev.sheldan.abstracto.core.converter;

import dev.sheldan.abstracto.core.models.dto.ServerDto;
import dev.sheldan.abstracto.core.models.template.ServerModel;
import org.springframework.stereotype.Component;

@Component
public class ServerModelConverter {
    public ServerModel fromServer(ServerDto server) {
        return ServerModel.builder().id(server.getId()).name(server.getName()).build();
    }
}
