package dev.sheldan.abstracto.moderation.models.template.job;

import dev.sheldan.abstracto.moderation.models.database.Warning;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;

@Getter
@Setter
@Builder
public class WarnDecayWarning {
    private Warning warning;
    private Member warnedMember;
    private Member warningMember;
}
