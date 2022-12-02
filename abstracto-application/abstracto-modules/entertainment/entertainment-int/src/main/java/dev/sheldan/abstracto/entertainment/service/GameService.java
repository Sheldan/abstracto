package dev.sheldan.abstracto.entertainment.service;

import dev.sheldan.abstracto.entertainment.model.command.games.MineBoard;
import net.dv8tion.jda.api.entities.Message;

public interface GameService {
    MineBoard createBoard(Integer width, Integer height, Integer mines, Long serverId);
    void persistMineBoardMessage(MineBoard mineBoard, Message message);
    void updateMineBoard(MineBoard mineBoard);
    void uncoverBoard(MineBoard mineBoard);
    void evaluateCreditChanges(MineBoard mineBoard);
    MineResult uncoverField(MineBoard board, Integer x, Integer y);


    enum MineResult {
        WON, LOST, CONTINUE
    }
}
