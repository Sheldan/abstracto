package dev.sheldan.abstracto.core.interaction.menu.listener;

import dev.sheldan.abstracto.core.Prioritized;
import dev.sheldan.abstracto.core.interaction.InteractionListener;
import dev.sheldan.abstracto.core.listener.FeatureAwareListener;

public interface StringSelectMenuListener extends FeatureAwareListener<StringSelectMenuListenerModel, StringSelectMenuListenerResult>, Prioritized, InteractionListener {
    Boolean handlesEvent(StringSelectMenuListenerModel model);
}
