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
     * The global unique ID of the {@link net.dv8tion.jda.api.entities.Emote}
     */
    private Long emoteId;
    /**
     * The name of the {@link net.dv8tion.jda.api.entities.Emote} in Discord
     */
    private String emoteName;
    /**
     * Whether or not the {@link net.dv8tion.jda.api.entities.Emote} is animated
     */
    private Boolean animated;
    /**
     * Whether or not the emote is from the {@link net.dv8tion.jda.api.entities.Guild} the {@link net.dv8tion.jda.api.entities.Message}
     * has been received on
     */
    private Boolean external;
    /**
     * Only if the emote is external: the URL where the source image of the {@link net.dv8tion.jda.api.entities.Emote} is stored on Discord servers
     */
    private String externalUrl;
    /**
     * The amount of times the {@link net.dv8tion.jda.api.entities.Emote} has been used.
     */
    private Long count;
    /**
     * The ID of the {@link net.dv8tion.jda.api.entities.Guild} on which the {@link net.dv8tion.jda.api.entities.Emote} has been used on
     */
    private Long serverId;
}
