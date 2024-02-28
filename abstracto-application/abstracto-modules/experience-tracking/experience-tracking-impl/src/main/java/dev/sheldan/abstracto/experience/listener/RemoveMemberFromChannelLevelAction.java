package dev.sheldan.abstracto.experience.listener;

import com.google.gson.Gson;
import dev.sheldan.abstracto.core.utils.ParseUtils;
import dev.sheldan.abstracto.experience.model.RemoveMemberFromChannelLevelActionPayload;
import dev.sheldan.abstracto.experience.model.database.AUserExperience;
import dev.sheldan.abstracto.experience.model.database.LevelAction;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RemoveMemberFromChannelLevelAction implements LevelActionListener {

    public static final String REMOVE_MEMBER_FROM_CHANNEL_ABOVE_LEVEL = "remove_member_from_channel_above_level";

    @Autowired
    private Gson gson;

    @Override
    public void apply(AUserExperience userExperience, LevelAction levelAction, MemberActionModification container) {
        RemoveMemberFromChannelLevelActionPayload payload = (RemoveMemberFromChannelLevelActionPayload) levelAction.getLoadedPayload();
        log.info("Removing member {} from channel {} in server {}.", userExperience.getUser().getUserReference().getId(), payload.getChannelId(), userExperience.getServer().getId());
        container.getChannelsToRemove().add(payload.getChannelId());
        container.getChannelsToAdd().remove(payload.getChannelId());
    }

    @Override
    public void prepareAction(LevelAction levelAction) {
        levelAction.setLoadedPayload(gson.fromJson(levelAction.getPayload(), RemoveMemberFromChannelLevelActionPayload.class));
    }

    @Override
    public RemoveMemberFromChannelLevelActionPayload createPayload(Guild guild, String input) {
        GuildChannel channel = ParseUtils.parseGuildChannelFromText(input, guild);
        return RemoveMemberFromChannelLevelActionPayload
                .builder()
                .channelId(channel.getIdLong())
                .build();
    }

    @Override
    public String getName() {
        return REMOVE_MEMBER_FROM_CHANNEL_ABOVE_LEVEL;
    }

    @Override
    public boolean shouldExecute(AUserExperience aUserExperience, LevelAction levelAction) {
        return aUserExperience.getLevelOrDefault() >= levelAction.getLevel().getLevel();
    }

}
