package dev.sheldan.abstracto.experience.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class LeaderBoard {
    private List<LeaderBoardEntry> entries;
}
