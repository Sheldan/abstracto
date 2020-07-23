package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.templating.Templatable;

import java.util.HashMap;
import java.util.List;

public class EmoteNotFoundInDbException extends AbstractoRunTimeException implements Templatable {

    private final Integer emoteId;

    public EmoteNotFoundInDbException(Integer emoteId) {
        super("");
        this.emoteId = emoteId;
    }

    @Override
    public String getTemplateName() {
        return "emote_not_found_in_db_exception";
    }

    @Override
    public Object getTemplateModel() {
        HashMap<String, Object> param = new HashMap<>();
        param.put("emoteId", this.emoteId);
        return param;
    }
}
