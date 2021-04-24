package dev.sheldan.abstracto.logging.model.template;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;

@Getter
@Setter
@Builder
public class MemberJoinLogModel {
    private Member member;
}
