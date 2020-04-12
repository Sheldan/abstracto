package dev.sheldan.abstracto.experience.models;

import dev.sheldan.abstracto.experience.models.database.AUserExperience;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LeaderBoardEntry {
    private AUserExperience experience;
    private Integer rank;
}
