package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.listener.MemberJoinModel;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
    private ListenerService listenerService;

    @Override
    @Transactional
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        if(listenerList == null) return;
        MemberJoinModel model = getModel(event);
        listenerList.forEach(joinListener -> listenerService.executeFeatureAwareListener(joinListener, model));
    }

    private MemberJoinModel getModel(GuildMemberJoinEvent event) {
        ServerUser serverUser = ServerUser
                .builder()
                .serverId(event.getGuild().getIdLong())
                .userId(event.getUser().getIdLong())
                .isBot(event.getUser().isBot())
                .build();
        return MemberJoinModel
                .builder()
                .joiningUser(serverUser)
                .member(event.getMember())
                .build();
    }

    @PostConstruct
    public void postConstruct() {
        BeanUtils.sortPrioritizedListeners(listenerList);
    }
}
