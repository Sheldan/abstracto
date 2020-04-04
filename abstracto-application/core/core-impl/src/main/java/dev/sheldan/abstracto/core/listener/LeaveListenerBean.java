package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.List;

@Service
@Slf4j
public class LeaveListenerBean extends ListenerAdapter {

    @Autowired
    private List<LeaveListener> listenerList;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Override
    @Transactional
    public void onGuildMemberLeave(@Nonnull GuildMemberLeaveEvent event) {
        listenerList.forEach(leaveListener -> {
            if(!featureFlagService.isFeatureEnabled(leaveListener.getFeature(), event.getGuild().getIdLong())) {
                return;
            }
            try {
                leaveListener.execute(event.getMember(), event.getGuild());
            } catch (AbstractoRunTimeException e) {
                log.error("Listener {} failed with exception:", leaveListener.getClass().getName(), e);
            }
        });
    }
}
