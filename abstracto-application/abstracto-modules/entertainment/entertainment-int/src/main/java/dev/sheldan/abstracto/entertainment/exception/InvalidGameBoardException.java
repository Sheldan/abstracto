package dev.sheldan.abstracto.entertainment.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;
import dev.sheldan.abstracto.entertainment.model.exception.InvalidGameBoardExceptionModel;

public class InvalidGameBoardException extends AbstractoTemplatableException {

    private final InvalidGameBoardExceptionModel model;

    public InvalidGameBoardException(Double minRatio) {
        super();
        this.model = InvalidGameBoardExceptionModel
                .builder()
                .minMinesRatio(minRatio)
                .build();
    }

    @Override
    public String getTemplateName() {
        return "invalid_mine_board_config_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
