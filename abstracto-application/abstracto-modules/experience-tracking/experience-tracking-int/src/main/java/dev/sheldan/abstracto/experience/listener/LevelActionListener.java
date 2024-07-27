package dev.sheldan.abstracto.experience.listener;

import dev.sheldan.abstracto.experience.model.LevelActionPayload;
import dev.sheldan.abstracto.experience.model.database.AUserExperience;
import dev.sheldan.abstracto.experience.model.database.LevelAction;
import net.dv8tion.jda.api.entities.Guild;

public interface LevelActionListener {
    String getName();

    void apply(AUserExperience userExperience, LevelAction levelAction, MemberActionModification container);

    boolean shouldExecute(AUserExperience aUserExperience, LevelAction levelAction);

    void prepareAction(LevelAction levelAction);


    LevelActionPayload createPayload(Guild guild, String input);
}
