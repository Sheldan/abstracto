package dev.sheldan.abstracto.experience.models.templates;

import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
public class LeaderBoardModel extends UserInitiatedServerContext {
    private List<LeaderBoardEntryModel> userExperiences;
    private LeaderBoardEntryModel userExecuting;
}
