package dev.sheldan.abstracto.modmail.model.database;

public enum ModMailThreadState {
    /**
     * User opened the mod mail thread or staff contacted member, but did not post a message yet
     */
    INITIAL,
    /**
     * User replied to mod mail thread
     */
    USER_REPLIED,
    /**
     * Staff member responded to the mod mail thread
     */
    MOD_REPLIED,
    /**
     * The thread was closed by a staff member and the channel was removed
     */
    CLOSED, CLOSING;
}
