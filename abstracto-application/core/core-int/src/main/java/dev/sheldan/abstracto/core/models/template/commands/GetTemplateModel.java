package dev.sheldan.abstracto.core.models.template.commands;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class GetTemplateModel {
    private String templateKey;
    private String templateContent;
    private Instant lastModified;
    private Instant created;
}
