package dev.sheldan.abstracto.entertainment.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;

public class InvalidGameBoardException extends AbstractoTemplatableException {
    @Override
    public String getTemplateName() {
        return "invalid_mine_board_config_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
