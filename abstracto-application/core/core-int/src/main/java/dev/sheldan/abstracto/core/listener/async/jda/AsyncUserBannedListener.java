package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.FeatureAwareListener;
import dev.sheldan.abstracto.core.models.listener.UserBannedListenerModel;

public interface AsyncUserBannedListener extends FeatureAwareListener<UserBannedListenerModel, DefaultListenerResult> {
}
