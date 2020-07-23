package dev.sheldan.abstracto.assignableroles.models.templates;


import dev.sheldan.abstracto.core.models.FullEmote;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AssignablePostRole {
    private FullEmote emote;
    private String description;
    private Integer position;
    @Builder.Default
    private Boolean forceNewMessage = false;
    private Boolean inline;
}
