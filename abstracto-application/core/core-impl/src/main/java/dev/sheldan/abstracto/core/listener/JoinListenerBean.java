package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Override
    @Transactional
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        listenerList.forEach(joinListener -> {
            FeatureConfig feature = featureFlagService.getFeatureDisplayForFeature(joinListener.getFeature());
            if (!featureFlagService.isFeatureEnabled(feature, event.getGuild().getIdLong())) {
                return;
            }
            try {
                AUserInAServer aUserInAServer = userInServerManagementService.loadUser(event.getMember());
                executeListener(event, joinListener, aUserInAServer);
            } catch (Exception e) {
                log.error("Listener {} failed with exception:", joinListener.getClass().getName(), e);
            }
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executeListener(@Nonnull GuildMemberJoinEvent event, JoinListener joinListener, AUserInAServer aUserInAServer) {
        joinListener.execute(event.getMember(), event.getGuild(), aUserInAServer);
    }
}
