package dev.sheldan.abstracto.utility.model;

import dev.sheldan.abstracto.core.models.context.SlimUserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Member;

import java.time.Instant;

@Getter
@Setter
@SuperBuilder
public class UserInfoModel extends SlimUserInitiatedServerContext {
    private Member  memberInfo;
    private Instant joinDate;
    private Instant creationDate;
}
