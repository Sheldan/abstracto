package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.utils.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
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
public class LeaveListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<LeaveListener> listenerList;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private LeaveListenerBean self;

    @Override
    @Transactional
    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {
        if(listenerList == null) return;
        listenerList.forEach(leaveListener -> {
            FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(leaveListener.getFeature());
            if(!featureFlagService.isFeatureEnabled(feature, event.getGuild().getIdLong())) {
                return;
            }
            try {
                self.executeIndividualLeaveListener(event, leaveListener);
            } catch (AbstractoRunTimeException e) {
                log.error("Listener {} failed with exception:", leaveListener.getClass().getName(), e);
            }
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public void executeIndividualLeaveListener(@Nonnull GuildMemberRemoveEvent event, LeaveListener leaveListener) {
        log.trace("Executing leave listener {} for member {} in guild {}.", leaveListener.getClass().getName(), event.getMember().getId(), event.getGuild().getId());
        leaveListener.execute(event.getMember(), event.getGuild());
    }

    @PostConstruct
    public void postConstruct() {
        BeanUtils.sortPrioritizedListeners(listenerList);
    }
}
