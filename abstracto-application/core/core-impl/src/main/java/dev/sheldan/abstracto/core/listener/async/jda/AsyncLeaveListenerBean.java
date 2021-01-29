package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class AsyncLeaveListenerBean extends ListenerAdapter {
    @Autowired(required = false)
    private List<AsyncLeaveListener> listenerList;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    @Qualifier("leaveListenerExecutor")
    private TaskExecutor leaveListenerExecutor;

    @Autowired
    private AsyncLeaveListenerBean self;

    @Override
    @Transactional
    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {
        if(listenerList == null) return;
        listenerList.forEach(leaveListener -> {
            ServerUser serverUser = ServerUser
                    .builder()
                    .userId(event.getUser().getIdLong())
                    .serverId(event.getGuild().getIdLong())
                    .build();
            CompletableFuture.runAsync(() ->
                self.executeIndividualLeaveListener(serverUser, leaveListener)
            , leaveListenerExecutor).exceptionally(throwable -> {
                log.error("Async leave listener {} threw exception.", leaveListener, throwable);
                return null;
            });

        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public void executeIndividualLeaveListener(ServerUser user, AsyncLeaveListener leaveListener) {
        log.trace("Executing leave listener {} for member {} in guild {}.", leaveListener.getClass().getName(), user.getUserId(), user.getServerId());
        FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(leaveListener.getFeature());
        if(!featureFlagService.isFeatureEnabled(feature, user.getServerId())) {
            return;
        }
        try {
            leaveListener.execute(user);
        } catch (AbstractoRunTimeException e) {
            log.error("Listener {} failed with exception:", leaveListener.getClass().getName(), e);
        }
    }

}
