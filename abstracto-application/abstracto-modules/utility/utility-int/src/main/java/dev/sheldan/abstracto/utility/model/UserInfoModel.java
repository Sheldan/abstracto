package dev.sheldan.abstracto.utility.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;

import java.time.Instant;

@Getter
@Setter
@Builder
public class UserInfoModel {
    private Member  memberInfo;
    private Instant joinDate;
    private Instant creationDate;
}
