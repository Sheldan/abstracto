package dev.sheldan.abstracto.assignableroles.models.exception;

import dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlace;
import dev.sheldan.abstracto.core.models.FullUser;
import dev.sheldan.abstracto.templating.Templatable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AssignableRolePlaceExceptionModel {
    private AssignableRolePlace rolePlace;
    private FullUser user;
    private Throwable throwable;

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
