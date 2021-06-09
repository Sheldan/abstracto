package dev.sheldan.abstracto.core.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;

import java.io.Serializable;

@Setter
@Getter
@Builder
public class MemberDisplayModel implements Serializable {
    private Long userId;
    private transient Member member;
}
