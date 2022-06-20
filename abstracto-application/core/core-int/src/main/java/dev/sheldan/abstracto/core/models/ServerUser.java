package dev.sheldan.abstracto.core.models;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;

import java.io.Serializable;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class ServerUser implements Serializable {
    private Long serverId;
    private Long userId;
    private Boolean isBot;

    public static ServerUser fromAUserInAServer(AUserInAServer aUserInAServer) {
        return ServerUser
                .builder()
                .serverId(aUserInAServer.getServerReference().getId())
                .userId(aUserInAServer.getUserReference().getId()).build();
    }

    public static ServerUser fromMember(Member member) {
        return ServerUser
                .builder()
                .serverId(member.getGuild().getIdLong())
                .userId(member.getIdLong())
                .isBot(member.getUser().isBot())
                .build();
    }
}
