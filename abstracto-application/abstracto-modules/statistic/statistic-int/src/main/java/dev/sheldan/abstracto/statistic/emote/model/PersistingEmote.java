package dev.sheldan.abstracto.statistic.emote.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * This model is used to store the usages of an {@link dev.sheldan.abstracto.statistic.emote.model.database.TrackedEmote} in runtime.
 * It does not store JDA related entities, but rather direct values.
 */
@Getter
@Setter
@Builder
public class PersistingEmote {
    /**
     * The global unique ID of the emote
     */
    private Long emoteId;
    /**
     * The name of the emote in Discord
     */
    private String emoteName;
    /**
     * Whether or not the emote is animated
     */
    private Boolean animated;
    /**
     * Whether or not the emote is from the {@link net.dv8tion.jda.api.entities.Guild} the {@link net.dv8tion.jda.api.entities.Message}
     * has been received on
     */
    private Boolean external;
    /**
     * Only if the emote is external: the URL where the source image of the emote is stored on Discord servers
     */
    private String externalUrl;
    /**
     * The amount of times the emote has been used.
     */
    private Long count;
    /**
     * The ID of the {@link net.dv8tion.jda.api.entities.Guild} on which the emote has been used on
     */
    private Long serverId;
}
