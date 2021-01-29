package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
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
public class AsyncJoinListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<AsyncJoinListener> listenerList;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private AsyncJoinListenerBean self;

    @Autowired
    @Qualifier("joinListenerExecutor")
    private TaskExecutor joinListenerExecutor;

    @Override
    @Transactional
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        if(listenerList == null) return;
        listenerList.forEach(joinListener -> {
            ServerUser serverUser = ServerUser
                    .builder()
                    .serverId(event.getGuild().getIdLong())
                    .userId(event.getUser().getIdLong())
                    .build();
            CompletableFuture.runAsync(() ->
                self.executeIndividualJoinListener(joinListener, serverUser)
            , joinListenerExecutor).exceptionally(throwable -> {
                log.error("Async join listener {} failed with exception.", joinListener, throwable);
                return null;
            });
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public void executeIndividualJoinListener(AsyncJoinListener joinListener, ServerUser serverUser) {
        FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(joinListener.getFeature());
        if (!featureFlagService.isFeatureEnabled(feature, serverUser.getServerId())) {
            return;
        }
        log.trace("Executing async join listener {} for user {} in server {}.", joinListener, serverUser.getServerId(), serverUser.getUserId());
        try {
            joinListener.execute(serverUser);
        } catch (Exception e) {
            log.error("Listener {} failed with exception:", joinListener.getClass().getName(), e);
        }
    }

}
