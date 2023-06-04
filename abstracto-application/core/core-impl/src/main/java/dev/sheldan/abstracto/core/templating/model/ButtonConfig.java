package dev.sheldan.abstracto.core.templating.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ButtonConfig {
    private String label;
    private String id;
    private String url;
    private Boolean disabled;
    private String emoteMarkdown;
    private ButtonStyleConfig buttonStyle;
    private String buttonPayload;
    private String payloadType;
    private ButtonMetaConfig metaConfig;
    private Integer position;
}
