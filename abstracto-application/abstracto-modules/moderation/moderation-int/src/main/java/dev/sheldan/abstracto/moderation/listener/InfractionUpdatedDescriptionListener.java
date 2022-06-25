package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.Prioritized;
import dev.sheldan.abstracto.core.listener.AsyncFeatureAwareListener;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.moderation.model.listener.InfractionDescriptionEventModel;

public interface InfractionUpdatedDescriptionListener extends AsyncFeatureAwareListener<InfractionDescriptionEventModel, DefaultListenerResult>, Prioritized {
    Boolean handlesEvent(InfractionDescriptionEventModel model);
}
