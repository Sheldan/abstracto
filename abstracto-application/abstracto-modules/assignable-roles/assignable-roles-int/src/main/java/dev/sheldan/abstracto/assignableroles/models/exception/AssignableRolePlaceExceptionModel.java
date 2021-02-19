package dev.sheldan.abstracto.assignableroles.models.exception;

import dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlace;
import dev.sheldan.abstracto.core.models.FullUserInServer;
import dev.sheldan.abstracto.core.templating.Templatable;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
public class AssignableRolePlaceExceptionModel implements Serializable {
    private final AssignableRolePlace rolePlace;
    private final FullUserInServer user;
    private final Throwable throwable;

    public Templatable getTemplate() {
        Throwable current = throwable;
        while(!(current instanceof Templatable) && !current.getCause().equals(current)) {
            current = current.getCause();
        }
        if(current instanceof Templatable) {
            return (Templatable) current;
        }
        return null;
    }
}
