package dev.sheldan.abstracto.modmail.models.database;

public enum ModMailThreadState {
    INITIAL, USER_REPLIED, MOD_REPLIED, CLOSED, CLOSING;

    public static ModMailThreadState getState(ModMailThreadState type) {
        switch (type) {
            case INITIAL: return ModMailThreadState.INITIAL;
            case USER_REPLIED: return ModMailThreadState.USER_REPLIED;
            case CLOSED: return ModMailThreadState.CLOSED;
            case CLOSING: return ModMailThreadState.CLOSING;
            default:
            case MOD_REPLIED: return ModMailThreadState.MOD_REPLIED;
        }
    }
}
