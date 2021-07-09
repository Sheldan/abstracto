package dev.sheldan.abstracto.assignableroles.model.template;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AssignablePostRole {
    private String emoteMarkDown;
    private String description;
    private String componentId;
}
