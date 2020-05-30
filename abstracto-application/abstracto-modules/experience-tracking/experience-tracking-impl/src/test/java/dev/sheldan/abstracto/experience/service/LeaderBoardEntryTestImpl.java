package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.experience.models.database.LeaderBoardEntryResult;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LeaderBoardEntryTestImpl implements LeaderBoardEntryResult {

    private Long id;
    private Long userInServerId;
    private Long experience;
    private Integer level;
    private Long messageCount;
    private Integer rank;
}
