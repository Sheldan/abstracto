package dev.sheldan.abstracto.core.interactive;


public interface DelayedAction {
    void execute(DelayedActionConfig delayedActionConfig);
    boolean handles(DelayedActionConfig delayedActionConfig);
}
