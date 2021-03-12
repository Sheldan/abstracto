package dev.sheldan.abstracto.experience.model.database;

/**
 * The object returned from the rank retrieval query.
 */
public interface LeaderBoardEntryResult {

    Long getId();

    /**
     * The {@link dev.sheldan.abstracto.core.models.database.AUserInAServer} id of the user
     * @return The ID of the user in a server
     */
    Long getUserInServerId();

    /**
     * The experience of the {@link dev.sheldan.abstracto.core.models.database.AUserInAServer}
     */
    Long getExperience();

    /**
     * The current raw level of the {@link dev.sheldan.abstracto.core.models.database.AUserInAServer}
     */
    Integer getLevel();

    /**
     * The amount of messages tracked by the {@link dev.sheldan.abstracto.core.models.database.AUserInAServer}
     */
    Long getMessageCount();

    /**
     * The current position of the {@link dev.sheldan.abstracto.core.models.database.AUserInAServer} in the leader board
     * ordered by experience count
     */
    Integer getRank();
}
