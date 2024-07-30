package dev.sheldan.abstracto.experience.listener;

import com.google.gson.Gson;
import dev.sheldan.abstracto.core.utils.ParseUtils;
import dev.sheldan.abstracto.experience.model.*;
import dev.sheldan.abstracto.experience.model.database.AUserExperience;
import dev.sheldan.abstracto.experience.model.database.LevelAction;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RemoveRoleLevelAction implements LevelActionListener {

    public static final String REMOVE_ROLE_ABOVE_LEVEL = "remove_role_above_level";

    @Autowired
    private Gson gson;

    @Override
    public void apply(AUserExperience userExperience, LevelAction levelAction, MemberActionModification container) {
        RemoveRoleLevelActionPayload payload = (RemoveRoleLevelActionPayload) levelAction.getLoadedPayload();
        log.info("Removing role {} from user {} in server {}.", payload.getRoleId(), userExperience.getUser().getUserReference().getId(), userExperience.getServer().getId());
        container.getRolesToRemove().add(payload.getRoleId());
        container.getRolesToAdd().remove(payload.getRoleId());
    }


    @Override
    public void prepareAction(LevelAction levelAction) {
        levelAction.setLoadedPayload(gson.fromJson(levelAction.getPayload(), RemoveRoleLevelActionPayload.class));
    }

    @Override
    public LevelActionPayload createPayload(Guild guild, String input) {
        Role role = ParseUtils.parseRoleFromText(input, guild);
        return RemoveRoleLevelActionPayload
                .builder()
                .roleId(role.getIdLong())
                .build();
    }

    @Override
    public String getName() {
        return REMOVE_ROLE_ABOVE_LEVEL;
    }

    @Override
    public boolean shouldExecute(AUserExperience aUserExperience, Integer oldLevel, LevelAction levelAction) {
        return aUserExperience.getLevelOrDefault() >= levelAction.getLevel().getLevel();
    }

}
