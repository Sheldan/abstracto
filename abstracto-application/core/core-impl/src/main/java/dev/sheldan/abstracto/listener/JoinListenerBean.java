package dev.sheldan.abstracto.listener;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.listener.JoinListener;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.List;

@Service
@Slf4j
public class JoinListenerBean extends ListenerAdapter {

    @Autowired
    private List<JoinListener> listenerList;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Override
    @Transactional
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        listenerList.forEach(joinListener -> {
            if (!featureFlagService.isFeatureEnabled(joinListener.getFeature(), event.getGuild().getIdLong())) {
                return;
            }
            try {
                joinListener.execute(event.getMember(), event.getGuild());
            } catch (AbstractoRunTimeException e) {
                log.error("Listener {} failed with exception:", joinListener.getClass().getName(), e);
            }
        });
    }
}
