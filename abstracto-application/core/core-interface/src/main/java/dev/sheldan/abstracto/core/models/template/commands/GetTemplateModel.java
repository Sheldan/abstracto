package dev.sheldan.abstracto.core.models.template.commands;

import dev.sheldan.abstracto.core.models.context.SlimUserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Getter
@Setter
@SuperBuilder
public class GetTemplateModel extends SlimUserInitiatedServerContext {
    private String templateKey;
    private String templateContent;
    private Instant lastModified;
    private Instant created;
}
