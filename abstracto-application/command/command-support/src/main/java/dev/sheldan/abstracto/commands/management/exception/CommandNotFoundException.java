package dev.sheldan.abstracto.commands.management.exception;

import dev.sheldan.abstracto.command.TemplatedException;

public class CommandNotFoundException extends RuntimeException implements TemplatedException {

    public CommandNotFoundException(String s) {
        super(s);
    }

    @Override
    public String getTemplateName() {
        return "command_not_found";
    }

    @Override
    public Object getTemplateModel() {
        return null;
    }
}
