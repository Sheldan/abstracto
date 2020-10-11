package dev.sheldan.abstracto.core.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;


@Getter
@Setter
@Builder
public class MemberPair {
    private Member firstMember;
    private Member secondMember;
}
