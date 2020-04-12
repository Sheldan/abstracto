package dev.sheldan.abstracto.experience.models.templates;

import dev.sheldan.abstracto.experience.models.database.AUserExperience;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;

@Getter
@Setter
@Builder
public class LeaderBoardEntryModel {
    private AUserExperience experience;
    private Member member;
    private Integer rank;
}
