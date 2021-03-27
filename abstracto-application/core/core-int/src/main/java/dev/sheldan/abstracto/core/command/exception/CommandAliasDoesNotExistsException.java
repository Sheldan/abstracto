package dev.sheldan.abstracto.core.command.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CommandAliasDoesNotExistsException extends AbstractoRunTimeException implements Templatable {

    @Override
    public String getTemplateName() {
        return "command_in_server_alias_not_exists_exists_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
