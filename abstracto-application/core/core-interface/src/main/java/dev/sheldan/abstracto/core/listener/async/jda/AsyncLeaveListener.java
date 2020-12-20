package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.FeatureAware;
import dev.sheldan.abstracto.core.models.ServerUser;

public interface AsyncLeaveListener extends FeatureAware {
    void execute(ServerUser serverUser);
}
