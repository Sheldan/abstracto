package dev.sheldan.abstracto.core.templating.model.messagecomponents;


import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class ButtonConfig {
    private String label;
    private String id;
    private String url;
    private Boolean disabled;
    private Integer uniqueId;
    private String emoteMarkdown;
    private ButtonStyleConfig buttonStyle;
    private String buttonPayload;
    private String payloadType;
    private ButtonMetaConfig metaConfig;
    private Integer position;
}
