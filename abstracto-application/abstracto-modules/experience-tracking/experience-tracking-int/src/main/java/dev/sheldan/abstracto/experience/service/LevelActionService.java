package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.experience.listener.LevelActionListener;
import dev.sheldan.abstracto.experience.model.database.AUserExperience;
import dev.sheldan.abstracto.experience.model.database.LevelAction;
import dev.sheldan.abstracto.experience.model.template.LevelActionsDisplay;
import net.dv8tion.jda.api.entities.Guild;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface LevelActionService {
    CompletableFuture<Void> applyLevelActionsToUser(AUserExperience user);
    List<String> getAvailableLevelActions();
    Optional<LevelActionListener> getLevelActionListenerForName(String name);

    Optional<LevelAction> getLevelAction(AUserExperience userExperience, String action, Integer level);

    LevelActionsDisplay getLevelActionsToDisplay(Guild guild);
}
