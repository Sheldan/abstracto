package dev.sheldan.abstracto.experience.listener;

import com.google.gson.Gson;
import dev.sheldan.abstracto.core.utils.ParseUtils;
import dev.sheldan.abstracto.experience.model.*;
import dev.sheldan.abstracto.experience.model.database.AUserExperience;
import dev.sheldan.abstracto.experience.model.database.LevelAction;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AddMemberToChannelLevelAction implements LevelActionListener {

    public static final String ADD_MEMBER_TO_CHANNEL_ABOVE_LEVEL = "add_member_to_channel_above_level";

    @Autowired
    private Gson gson;

    @Override
    public void apply(AUserExperience userExperience, LevelAction levelAction, MemberActionModification container) {
        AddMemberToChannelLevelActionPayload payload = (AddMemberToChannelLevelActionPayload) levelAction.getLoadedPayload();
        log.info("Adding member {} to channel {} in server {}.", userExperience.getUser().getUserReference().getId(), payload.getChannelId(), userExperience.getServer().getId());
        container.getChannelsToAdd().add(payload.getChannelId());
        container.getChannelsToRemove().remove(payload.getChannelId());
    }

    @Override
    public void prepareAction(LevelAction levelAction) {
        levelAction.setLoadedPayload(gson.fromJson(levelAction.getPayload(), AddMemberToChannelLevelActionPayload.class));
    }

    @Override
    public AddMemberToChannelLevelActionPayload createPayload(Guild guild, String input) {
        GuildChannel channel = ParseUtils.parseGuildChannelFromText(input, guild);
        return AddMemberToChannelLevelActionPayload
                .builder()
                .channelId(channel.getIdLong())
                .build();
    }

    @Override
    public String getName() {
        return ADD_MEMBER_TO_CHANNEL_ABOVE_LEVEL;
    }

    @Override
    public boolean shouldExecute(AUserExperience aUserExperience, LevelAction levelAction) {
        return aUserExperience.getLevelOrDefault() >= levelAction.getLevel().getLevel();
    }

}
