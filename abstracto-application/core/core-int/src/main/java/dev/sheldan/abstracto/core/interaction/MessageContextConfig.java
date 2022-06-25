package dev.sheldan.abstracto.core.interaction;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MessageContextConfig {
    private String templateKey;
    private String name;
    private Boolean isTemplated;
}
