package dev.sheldan.abstracto.core.command.models.exception;

import dev.sheldan.abstracto.core.models.FullUser;
import dev.sheldan.abstracto.templating.Templatable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GenericExceptionModel {
    private FullUser user;
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
