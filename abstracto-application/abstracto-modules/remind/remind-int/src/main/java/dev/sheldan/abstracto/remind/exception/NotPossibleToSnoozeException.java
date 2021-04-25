package dev.sheldan.abstracto.remind.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

public class NotPossibleToSnoozeException extends AbstractoRunTimeException implements Templatable {

    public NotPossibleToSnoozeException() {
        super("Reminder has not yet finished. Not possible to snooze");
    }

    @Override
    public String getTemplateName() {
        return "reminder_snooze_not_possible_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
