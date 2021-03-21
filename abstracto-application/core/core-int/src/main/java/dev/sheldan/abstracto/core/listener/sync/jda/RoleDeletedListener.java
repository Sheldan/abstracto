package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.Prioritized;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.FeatureAwareListener;
import dev.sheldan.abstracto.core.models.listener.RoleDeletedModel;

public interface RoleDeletedListener extends FeatureAwareListener<RoleDeletedModel, DefaultListenerResult>, Prioritized {
}
