package dev.sheldan.abstracto.entertainment.model.command.games;

import dev.sheldan.abstracto.core.interaction.button.ButtonPayload;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@Setter
public class MineBoardPayload implements ButtonPayload {
    private MineBoard mineBoard;
    private Integer x;
    private Integer y;
}
