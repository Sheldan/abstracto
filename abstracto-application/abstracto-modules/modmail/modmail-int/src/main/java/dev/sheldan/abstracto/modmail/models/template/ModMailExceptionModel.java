package dev.sheldan.abstracto.modmail.models.template;

import dev.sheldan.abstracto.core.models.FullUser;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * This model is used to render any exception happening when executing a command within a {@link ModMailThread}
 * and this command failing in any capacity. This model is used to render multiple templates (for different kinds of
 * exceptions), all of which might use the information or not.
 */
@Getter
@Setter
@Builder
public class ModMailExceptionModel {
    /**
     * The {@link ModMailThread} in which the exception occurred
     */
    private ModMailThread modMailThread;
    /**
     * A user associated with this exception, depends on the exact behaviour of the exception.
     */
    private FullUser user;
    /**
     * The exception which was thrown
     */
    private Throwable throwable;
}
