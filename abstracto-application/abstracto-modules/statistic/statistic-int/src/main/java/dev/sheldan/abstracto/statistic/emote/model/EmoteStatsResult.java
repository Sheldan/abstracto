package dev.sheldan.abstracto.statistic.emote.model;

/**
 * The interface used to fill with values from the emote stats query. This represents the grouped result consisting of:
 * the ID of the emote, the ID of the server and the amount of times this emote has been used in the server
 */
public interface EmoteStatsResult {
    /**
     * @return The ID of the emote
     */
    Long getEmoteId();

    /**
     * @return  ID of the server
     */
    Long getServerId();

    /**
     * @return Amount the emote with the ID has been used in the server with the ID
     */
    Long getAmount();
}
