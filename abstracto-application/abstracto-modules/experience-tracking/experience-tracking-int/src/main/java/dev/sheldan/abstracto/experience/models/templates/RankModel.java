package dev.sheldan.abstracto.experience.models.templates;

import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class RankModel extends UserInitiatedServerContext {
    private LeaderBoardEntryModel rankUser;
    private Long experienceToNextLevel;
}
