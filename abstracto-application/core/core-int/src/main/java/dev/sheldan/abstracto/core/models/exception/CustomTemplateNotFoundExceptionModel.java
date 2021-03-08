package dev.sheldan.abstracto.core.models.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;

@Getter
@Setter
@Builder
public class CustomTemplateNotFoundExceptionModel {
    private String templateKey;
    private Guild guild;
}
