package dev.sheldan.abstracto.core.models.template.display;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.utils.MemberUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;

@Getter
@Setter
@Builder
public class MemberDisplay {
    private String memberMention;
    private Long userId;
    private Long serverId;

    public static MemberDisplay fromMember(Member member) {
        return MemberDisplay
                .builder()
                .memberMention(member.getAsMention())
                .serverId(member.getGuild().getIdLong())
                .userId(member.getIdLong())
                .build();
    }

    public static MemberDisplay fromAUserInAServer(AUserInAServer aUserInAServer) {
        return MemberDisplay
                .builder()
                .memberMention(MemberUtils.getAUserInAServerAsMention(aUserInAServer))
                .serverId(aUserInAServer.getServerReference().getId())
                .userId(aUserInAServer.getUserReference().getId())
                .build();
    }

    public static MemberDisplay fromIds(Long serverId, Long userId) {
        return MemberDisplay
                .builder()
                .memberMention(MemberUtils.getUserAsMention(userId))
                .serverId(serverId)
                .userId(userId)
                .build();
    }
}
