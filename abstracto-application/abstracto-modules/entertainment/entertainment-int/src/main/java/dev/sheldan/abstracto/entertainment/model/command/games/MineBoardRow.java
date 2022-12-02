package dev.sheldan.abstracto.entertainment.model.command.games;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MineBoardRow {
    private List<MineBoardField> fields;
}
