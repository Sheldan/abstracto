package dev.sheldan.abstracto.core.models;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class ServerUser {
    private Long serverId;
    private Long userId;

    public static ServerUser fromAUserInAServer(AUserInAServer aUserInAServer) {
        return ServerUser
                .builder()
                .serverId(aUserInAServer.getServerReference().getId())
                .userId(aUserInAServer.getUserReference().getId()).build();
    }
}
