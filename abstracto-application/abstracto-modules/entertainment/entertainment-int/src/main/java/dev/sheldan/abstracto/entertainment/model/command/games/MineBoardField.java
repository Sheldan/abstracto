package dev.sheldan.abstracto.entertainment.model.command.games;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@Setter
public class MineBoardField {
    private MineBoardField.MineBoardFieldType type;
    private Integer x;
    private Integer y;
    private Integer counterValue;

    public enum MineBoardFieldType {
        MINE, UNCOVERED, COVERED, EXPLODED
    }

    public static boolean canInteract(MineBoardFieldType currentType) {
        return currentType != MineBoardFieldType.UNCOVERED && currentType != MineBoardFieldType.EXPLODED;
    }

}

