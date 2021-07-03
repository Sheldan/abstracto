package dev.sheldan.abstracto.core.interaction;

import dev.sheldan.abstracto.core.templating.Templatable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

@Getter
@Setter
@Builder
public class GenericInteractionExceptionModel {
    private Member member;
    private User user;
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
