package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.listener.ServerConfigListener;
import dev.sheldan.abstracto.core.utils.SnowflakeUtils;
import dev.sheldan.abstracto.core.management.ChannelManagementService;
import dev.sheldan.abstracto.core.management.RoleManagementService;
import dev.sheldan.abstracto.core.management.ServerManagementService;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.AChannelType;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class StartupManager implements Startup {

    @Autowired
    private Bot service;

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
    @Transactional
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
            AServer newAServer = serverManagementService.createServer(aLong);
            Guild newGuild = instance.getGuildById(aLong);
            log.debug("Synchronizing server: {}", aLong);
            if(newGuild != null){
                synchronizeRolesOf(newGuild, newAServer);
                synchronizeChannelsOf(newGuild, newAServer);
                configListeners.forEach(serverConfigListener -> {
                    serverConfigListener.updateServerConfig(newAServer);
                });
            }
        });

    }

    private void synchronizeRolesOf(Guild guild, AServer existingAServer){
        List<Role> existingRoles = guild.getRoles();
        List<ARole> knownARoles = existingAServer.getRoles();
        Set<Long> knownRolesId = SnowflakeUtils.getOwnItemsIds(knownARoles);
        Set<Long> availableRoles = SnowflakeUtils.getSnowflakeIds(existingRoles);
        Set<Long> newRoles = SetUtils.disjunction(availableRoles, knownRolesId);
        newRoles.forEach(aLong -> {
            ARole newRole = roleManagementService.createRole(aLong);
            log.debug("Adding new role: {}", aLong);
            existingAServer.getRoles().add(newRole);
        });
    }

    private void synchronizeChannelsOf(Guild guild, AServer existingServer){
        List<GuildChannel> available = guild.getChannels();
        List<AChannel> knownChannels = existingServer.getChannels();
        Set<Long> knownChannelsIds = SnowflakeUtils.getOwnItemsIds(knownChannels);
        Set<Long> existingChannelsIds = SnowflakeUtils.getSnowflakeIds(available);
        Set<Long> newChannels = SetUtils.disjunction(existingChannelsIds, knownChannelsIds);
        newChannels.forEach(aLong -> {
            GuildChannel channel1 = available.stream().filter(channel -> channel.getIdLong() == aLong).findFirst().get();
            log.debug("Adding new channel: {}", aLong);
            AChannelType type = AChannel.getAChannelType(channel1.getType());
            AChannel newChannel = channelManagementService.createChannel(channel1.getIdLong(), type);
            serverManagementService.addChannelToServer(existingServer, newChannel);
        });
    }
}
