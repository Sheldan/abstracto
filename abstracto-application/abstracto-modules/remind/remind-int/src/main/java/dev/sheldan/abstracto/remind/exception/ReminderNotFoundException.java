package dev.sheldan.abstracto.remind.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;
import dev.sheldan.abstracto.remind.model.exception.ReminderNotFoundExceptionModel;

public class ReminderNotFoundException extends AbstractoRunTimeException implements Templatable {

    private final ReminderNotFoundExceptionModel model;
    public ReminderNotFoundException(Long reminderId) {
        super("Reminder does not exist");
        this.model = ReminderNotFoundExceptionModel.builder().reminderId(reminderId).build();
    }

    @Override
    public String getTemplateName() {
        return "reminder_does_not_exist_exception";
    }

    @Override
    public Object getTemplateModel() {
       return model;
    }
}
