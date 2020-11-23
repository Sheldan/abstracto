package dev.sheldan.abstracto.statistic.emotes.model;

/**
 * The interface used to fill with values from the emote stats query. This represents the grouped result consisting of:
 * the ID of the emote, the ID of the server and the amount of times this emote has been used in the server
 */
public interface EmoteStatsResult {
    /**
     * ID of the emote
     */
    Long getEmoteId();

    /**
     * ID of the server
     */
    Long getServerId();

    /**
     * Amount the emote with the ID has been used in the server with the ID
     */
    Long getAmount();
}
