package dev.sheldan.abstracto.modmail.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.templating.Templatable;

import java.util.HashMap;

public class ModMailThreadNotFoundException extends AbstractoRunTimeException implements Templatable {

    private final Long modMailThreadId;

    public ModMailThreadNotFoundException(Long modMailThreadId) {
        super("");
        this.modMailThreadId = modMailThreadId;
    }

    @Override
    public String getTemplateName() {
        return "modmail_cannot_find_modmail_thread_exception_text";
    }

    @Override
    public Object getTemplateModel() {
        HashMap<String, Long> params = new HashMap<>();
        params.put("id", this.modMailThreadId);
        return params;
    }
}
