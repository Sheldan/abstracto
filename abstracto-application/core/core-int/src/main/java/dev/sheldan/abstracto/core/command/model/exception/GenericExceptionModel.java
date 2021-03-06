package dev.sheldan.abstracto.core.command.model.exception;

import dev.sheldan.abstracto.core.models.FullUser;
import dev.sheldan.abstracto.core.models.FullUserInServer;
import dev.sheldan.abstracto.core.templating.Templatable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GenericExceptionModel {
    private FullUserInServer user;
    private FullUser fullUser;
    private Throwable throwable;

    public Templatable getTemplate() {
        Throwable current = throwable;
        while(!(current instanceof Templatable) && (current.getCause() != null && !current.getCause().equals(current))) {
            current = current.getCause();
        }
        if(current instanceof Templatable) {
            return (Templatable) current;
        }
        return null;
    }
}
