package dev.sheldan.abstracto.entertainment.listener.interaction;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListener;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListenerModel;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListenerResult;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.entertainment.command.games.Mines;
import dev.sheldan.abstracto.entertainment.config.EntertainmentFeatureDefinition;
import dev.sheldan.abstracto.entertainment.model.command.games.MineBoard;
import dev.sheldan.abstracto.entertainment.model.command.games.MineBoardPayload;
import dev.sheldan.abstracto.entertainment.service.GameService;
import dev.sheldan.abstracto.entertainment.service.GameServiceBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class MinesButtonClickedListener implements ButtonClickedListener {

    @Autowired
    private GameService gameService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Override
    public ButtonClickedListenerResult execute(ButtonClickedListenerModel model) {
        MineBoardPayload payload = (MineBoardPayload) model.getDeserializedPayload();
        if(model.getEvent().getUser().getIdLong() != payload.getMineBoard().getUserId()) {
            return ButtonClickedListenerResult.IGNORED;
        }
        MineBoard mineBoard = payload.getMineBoard();
        if(!mineBoard.getState().equals(GameService.MineResult.CONTINUE)) {
            return ButtonClickedListenerResult.IGNORED;
        }
        GameService.MineResult mineResult = gameService.uncoverField(mineBoard, payload.getX(), payload.getY());
        mineBoard.setState(mineResult);
        if(mineBoard.getState() != GameService.MineResult.CONTINUE) {
            if(featureFlagService.getFeatureFlagValue(EntertainmentFeatureDefinition.ECONOMY, model.getServerId())){
                gameService.evaluateCreditChanges(mineBoard);
            }
            gameService.uncoverBoard(mineBoard);
        }
        MessageToSend messageToSend = templateService.renderEmbedTemplate(Mines.MINE_BOARD_TEMPLATE_KEY, mineBoard);
        interactionService.editOriginal(messageToSend, model.getEvent().getHook()).thenAccept(message -> {
            gameService.updateMineBoard(mineBoard);
            log.info("Updated original mineboard for board {}.", mineBoard.getBoardId());
        });
        return ButtonClickedListenerResult.ACKNOWLEDGED;
    }

    @Override
    public FeatureDefinition getFeature() {
        return EntertainmentFeatureDefinition.GAMES;
    }

    @Override
    public Integer getPriority() {
        return ListenerPriority.MEDIUM;
    }

    @Override
    public Boolean handlesEvent(ButtonClickedListenerModel model) {
        return model.getOrigin().equals(GameServiceBean.MINES_BUTTON_ORIGIN);
    }
}
