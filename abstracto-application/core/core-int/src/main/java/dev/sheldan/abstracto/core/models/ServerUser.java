package dev.sheldan.abstracto.core.models;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.*;
import net.dv8tion.jda.api.entities.Member;

import java.io.Serializable;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
public class ServerUser implements Serializable {
    private Long serverId;
    private Long userId;
    @EqualsAndHashCode.Exclude
    private Boolean isBot;

    public static ServerUser fromAUserInAServer(AUserInAServer aUserInAServer) {
        return ServerUser
                .builder()
                .serverId(aUserInAServer.getServerReference().getId())
                .userId(aUserInAServer.getUserReference().getId())
                .build();
    }

    public static ServerUser fromId(Long serverId, Long userId) {
        return ServerUser
                .builder()
                .serverId(serverId)
                .userId(userId)
                .build();
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
