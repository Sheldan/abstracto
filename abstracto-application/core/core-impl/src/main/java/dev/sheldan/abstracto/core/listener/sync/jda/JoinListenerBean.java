package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.List;

@Service
@Slf4j
public class JoinListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<JoinListener> listenerList;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private JoinListenerBean self;

    @Override
    @Transactional
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        if(listenerList == null) return;
        listenerList.forEach(joinListener -> {
            FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(joinListener.getFeature());
            if (!featureFlagService.isFeatureEnabled(feature, event.getGuild().getIdLong())) {
                return;
            }
            try {
                AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(event.getMember());
                self.executeIndividualJoinListener(event, joinListener, aUserInAServer);
            } catch (Exception e) {
                log.error("Listener {} failed with exception:", joinListener.getClass().getName(), e);
            }
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public void executeIndividualJoinListener(@Nonnull GuildMemberJoinEvent event, JoinListener joinListener, AUserInAServer aUserInAServer) {
        log.trace("Executing join listener {} for member {} in guild {}.", joinListener.getClass().getName(), event.getMember().getId(), event.getGuild().getId());
        joinListener.execute(event.getMember(), event.getGuild(), aUserInAServer);
    }

    @PostConstruct
    public void postConstruct() {
        BeanUtils.sortPrioritizedListeners(listenerList);
    }
}
