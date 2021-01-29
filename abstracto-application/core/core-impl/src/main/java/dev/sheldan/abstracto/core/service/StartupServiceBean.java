package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.listener.sync.entity.ServerConfigListener;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AChannelType;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.utils.SnowflakeUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.LoginException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StartupServiceBean implements Startup {

    @Autowired
    private BotService service;

    @Autowired
    private List<? extends  ListenerAdapter> listeners;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private RoleManagementService roleManagementService;

    @Autowired
    private List<ServerConfigListener> configListeners;


    @Override
    public void startBot() throws LoginException {
        service.login();
        listeners.forEach(o -> service.getInstance().addEventListener(o));
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void synchronize() {
        log.info("Synchronizing servers.");
        synchronizeServers();
        log.info("Done synchronizing servers");
    }

    private void synchronizeServers(){
        JDA instance = service.getInstance();
        List<Guild> onlineGuilds = instance.getGuilds();
        Set<Long> availableServers = SnowflakeUtils.getSnowflakeIds(onlineGuilds);
        availableServers.forEach(aLong -> {
            AServer newAServer = serverManagementService.loadOrCreate(aLong);
            Guild newGuild = instance.getGuildById(aLong);
            log.trace("Synchronizing server: {}", aLong);
            if(newGuild != null){
                synchronizeRolesOf(newGuild, newAServer);
                synchronizeChannelsOf(newGuild, newAServer);
                configListeners.forEach(serverConfigListener ->
                    serverConfigListener.updateServerConfig(newAServer)
                );
            }
        });

    }

    // TODO mark deleted roles ad deleted, use intersect for that
    private void synchronizeRolesOf(Guild guild, AServer existingAServer){
        List<Role> existingRoles = guild.getRoles();
        List<ARole> knownARoles = existingAServer.getRoles();
        Set<Long> knownRolesId = SnowflakeUtils.getOwnItemsIds(knownARoles);
        Set<Long> availableRoles = SnowflakeUtils.getSnowflakeIds(existingRoles);
        Set<Long> newRoles = SetUtils.difference(availableRoles, knownRolesId);
        newRoles.forEach(aLong -> {
            roleManagementService.createRole(aLong, existingAServer);
            log.trace("Adding new role: {}", aLong);
        });
    }

    private void synchronizeChannelsOf(Guild guild, AServer existingServer){
        List<GuildChannel> available = guild.getChannels();
        List<AChannel> knownChannels = existingServer.getChannels().stream().filter(aChannel -> !aChannel.getDeleted()).collect(Collectors.toList());
        Set<Long> knownChannelsIds = SnowflakeUtils.getOwnItemsIds(knownChannels);
        Set<Long> existingChannelsIds = SnowflakeUtils.getSnowflakeIds(available);
        Set<Long> newChannels = SetUtils.difference(existingChannelsIds, knownChannelsIds);
        newChannels.forEach(aLong -> {
            GuildChannel channel1 = available.stream().filter(channel -> channel.getIdLong() == aLong).findFirst().get();
            log.trace("Adding new channel: {}", aLong);
            AChannelType type = AChannelType.getAChannelType(channel1.getType());
            channelManagementService.createChannel(channel1.getIdLong(), type, existingServer);
        });

        Set<Long> noLongAvailable = SetUtils.difference(knownChannelsIds, existingChannelsIds);
        noLongAvailable.forEach(aLong ->
            channelManagementService.markAsDeleted(aLong)
        );
    }
}
