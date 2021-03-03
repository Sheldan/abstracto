package dev.sheldan.abstracto.assignableroles.models.templates;


import dev.sheldan.abstracto.core.models.FullEmote;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * The model which is used to render the {@link dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlacePost post}
 * to a {@link dev.sheldan.abstracto.core.templating.model.MessageToSend messageToSend} for one individual {@link dev.sheldan.abstracto.assignableroles.models.database.AssignableRole role}
 */
@Getter
@Setter
@Builder
public class AssignablePostRole {
    /**
     * The {@link FullEmote emote} to be used in the field
     */
    private FullEmote emote;
    /**
     * The description to be used in the field
     */
    private String description;
    /**
     * The relative position within the {@link dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlace place} of this
     * {@link dev.sheldan.abstracto.assignableroles.models.database.AssignableRole role}
     */
    private Integer position;
    /**
     * Whether or not this field should be at the beginning of a new {@link net.dv8tion.jda.api.entities.Message message}
     * to be send
     */
    @Builder.Default
    private Boolean forceNewMessage = false;
    /**
     * Whether or not the field should be displayed inline
     */
    private Boolean inline;
}
