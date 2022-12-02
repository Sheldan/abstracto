package dev.sheldan.abstracto.entertainment.model.command.games;

import dev.sheldan.abstracto.entertainment.service.GameService;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class MineBoard {
    private String boardId;
    private Long userId;
    private Long serverId;
    private Long messageId;
    private Long channelId;
    private Integer rowCount;
    private Integer columnCount;
    private Integer credits;
    private Long creditChange;
    private boolean creditsEnabled;
    private Integer mineCount;
    private List<MineBoardRow> rows;
    private GameService.MineResult state;

    public MineBoardField getField(int x, int y) {
        if(x > columnCount || y > rowCount) {
            throw new IllegalArgumentException("Out of bounds access to board.");
        }
        MineBoardRow mineBoardRow = rows.get(y);
        return mineBoardRow.getFields().get(x);
    }

    public List<MineBoardField> getFields() {
        List<MineBoardField> fields = new ArrayList<>();
        rows.forEach(mineBoardRow -> fields.addAll(mineBoardRow.getFields()));
        return fields;
    }


}
