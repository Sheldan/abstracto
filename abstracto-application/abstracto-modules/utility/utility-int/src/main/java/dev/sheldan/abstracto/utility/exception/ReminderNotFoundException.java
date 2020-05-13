package dev.sheldan.abstracto.utility.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.templating.Templatable;

import java.util.HashMap;

public class ReminderNotFoundException extends AbstractoRunTimeException implements Templatable {

    private Long reminderId;
    public ReminderNotFoundException(Long reminderId) {
        super("");
        this.reminderId = reminderId;
    }

    @Override
    public String getTemplateName() {
        return "reminder_does_not_exist_exception";
    }

    @Override
    public Object getTemplateModel() {
        HashMap<String, Long> params = new HashMap<>();
        params.put("id", this.reminderId);
        return params;
    }
}
