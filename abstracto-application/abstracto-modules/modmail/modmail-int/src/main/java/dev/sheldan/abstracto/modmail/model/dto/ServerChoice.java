package dev.sheldan.abstracto.modmail.model.dto;

import dev.sheldan.abstracto.core.models.FullGuild;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Used when the user shares multiple servers with the bot and needs to determine for which server the user
 * wants to open a mod mail thread, this is done by reacting to the prompt with the proper emote.
 */
@Getter
@Setter
@Builder
public class ServerChoice {
    /**
     * The possible guild to open a mod mail thread for
     */
    private FullGuild guild;
    /**
     * The unicode emote used in the prompt to identify this choice
     */
    private String reactionEmote;
}
